package com.github.jerpent.bankcleaner;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("bankcleaner")
public interface BankCleanerConfig extends Config
{
	@ConfigItem(
		keyName = "showUntradeables",
		name = "Show untradeables",
		description = "Flag untradeable items that are outclassed by better gear in your bank"
	)
	default boolean showUntradeables()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showDegradeables",
		name = "Show degradeables",
		description = "Show degradeable items that may need review"
	)
	default boolean showDegradeables()
	{
		return true;
	}

	@ConfigItem(
		keyName = "ignoredItemIds",
		name = "Ignored item IDs",
		description = "Comma-separated list of item IDs to hide from results"
	)
	default String ignoredItemIds()
	{
		return "";
	}

	@ConfigItem(
		keyName = "showIgnoredItems",
		name = "Show ignored items",
		description = "Show ignored items with strikethrough instead of hiding them"
	)
	default boolean showIgnoredItems()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showStatComparison",
		name = "Detailed stat tooltip",
		description = "Show a stat-by-stat comparison in the tooltip instead of just the item name"
	)
	default boolean showStatComparison()
	{
		return false;
	}
}
