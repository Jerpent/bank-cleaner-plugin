package com.github.jerpent.bankcleaner;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
	name = "Bank Cleaner",
	description = "Identifies redundant gear in your bank",
	tags = {"bank", "gear", "cleanup", "inventory"}
)
public class BankCleanerPlugin extends Plugin
{
	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ItemClassifier classifier;

	@Inject
	private ItemComparator comparator;

	@Inject
	private BankCleanerConfig config;

	@Inject
	private ConfigManager configManager;

	private BankCleanerPanel panel;
	private NavigationButton navButton;

	@Override
	protected void startUp()
	{
		panel = new BankCleanerPanel(this, config);

		BufferedImage icon;
		try
		{
			icon = ImageUtil.loadImageResource(getClass(), "icon.png");
		}
		catch (IllegalArgumentException e)
		{
			icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		}

		navButton = NavigationButton.builder()
			.tooltip("Bank Cleaner")
			.icon(icon)
			.priority(6)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown()
	{
		clientToolbar.removeNavigation(navButton);
		panel.reset();
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != InventoryID.BANK.getId())
		{
			return;
		}

		ItemContainer bank = event.getItemContainer();
		if (bank == null) return;

		scanBank(bank.getItems());
	}

	private void scanBank(Item[] items)
	{
		Set<Integer> ignored = getIgnoredIds();
		List<BankItem> equippable = new ArrayList<>();
		List<RedundantItem> degradeables = new ArrayList<>();

		for (Item item : items)
		{
			if (item.getId() <= 0) continue;

			String name = itemManager.getItemComposition(item.getId()).getName();

			if (classifier.isDegradeable(item.getId(), name) && config.showDegradeables())
			{
				degradeables.add(new RedundantItem(
					item.getId(), name,
					"Degradeable item — verify you need this in your bank",
					null,
					null,
					RedundantItem.Category.DEGRADEABLE,
					ignored.contains(item.getId())
				));
				continue;
			}

			if (!classifier.isEquippable(item.getId())) continue;

			ItemStats stats = classifier.getStats(item.getId());
			if (stats == null || stats.getEquipment() == null) continue;
			if (!comparator.hasAnyStats(stats)) continue; // skip cosmetic items with all-zero stats

			boolean tradeable = classifier.isTradeable(item.getId());
			ItemClassifier.WeaponSubtype subtype = classifier.getWeaponSubtype(item.getId(), name);
			equippable.add(new BankItem(item.getId(), name, stats, tradeable, subtype));
		}

		List<RedundantItem> results = new ArrayList<>();

		for (int i = 0; i < equippable.size(); i++)
		{
			BankItem candidate = equippable.get(i);

			for (int j = 0; j < equippable.size(); j++)
			{
				if (i == j) continue;
				BankItem other = equippable.get(j);

				if (comparator.isDominatedBy(candidate.stats, other.stats, candidate.subtype, other.subtype))
				{
					RedundantItem.Category category = candidate.tradeable
						? RedundantItem.Category.SELL
						: RedundantItem.Category.DROP;

					String statComparison = comparator.buildStatComparison(candidate.stats, other.stats, other.name);

					results.add(new RedundantItem(
						candidate.id,
						candidate.name,
						"Outclassed by " + other.name,
						other.name,
						statComparison,
						category,
						ignored.contains(candidate.id)
					));
					break;
				}
			}
		}

		results.addAll(degradeables);
		panel.update(results, config.showUntradeables(), config.showDegradeables());
	}

	public void ignoreItem(int itemId)
	{
		Set<Integer> ids = getIgnoredIds();
		ids.add(itemId);
		saveIgnoredIds(ids);
	}

	public void unignoreItem(int itemId)
	{
		Set<Integer> ids = getIgnoredIds();
		ids.remove(itemId);
		saveIgnoredIds(ids);
	}

	public void clearIgnoredItems()
	{
		configManager.setConfiguration("bankcleaner", "ignoredItemIds", "");
	}

	public Set<Integer> getIgnoredIds()
	{
		String raw = config.ignoredItemIds();
		if (raw == null || raw.isBlank()) return new HashSet<>();
		return Arrays.stream(raw.split(","))
			.map(String::trim)
			.filter(s -> !s.isEmpty())
			.map(Integer::parseInt)
			.collect(Collectors.toCollection(HashSet::new));
	}

	private void saveIgnoredIds(Set<Integer> ids)
	{
		String value = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
		configManager.setConfiguration("bankcleaner", "ignoredItemIds", value);
	}

	private static class BankItem
	{
		final int id;
		final String name;
		final ItemStats stats;
		final boolean tradeable;
		final ItemClassifier.WeaponSubtype subtype;

		BankItem(int id, String name, ItemStats stats, boolean tradeable, ItemClassifier.WeaponSubtype subtype)
		{
			this.id = id;
			this.name = name;
			this.stats = stats;
			this.tradeable = tradeable;
			this.subtype = subtype;
		}
	}

	@Provides
	BankCleanerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BankCleanerConfig.class);
	}
}
