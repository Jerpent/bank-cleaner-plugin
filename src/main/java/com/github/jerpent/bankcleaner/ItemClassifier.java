package com.github.jerpent.bankcleaner;

import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;
import net.runelite.client.game.ItemEquipmentStats;

import javax.inject.Inject;

public class ItemClassifier
{
	public enum WeaponSubtype
	{
		BOW, CROSSBOW, THROWN, STAFF, MELEE, NONE
	}

	// Weapon slot index in OSRS
	private static final int WEAPON_SLOT = 3;

	@Inject
	private ItemManager itemManager;

	public boolean isTradeable(int itemId)
	{
		ItemComposition comp = itemManager.getItemComposition(itemId);
		return comp.isTradeable();
	}

	public boolean isDegradeable(int itemId, String name)
	{
		return DegradeableItems.IDS.contains(itemId) || DegradeableItems.isBarrowsVariant(name);
	}

	public boolean isEquippable(int itemId)
	{
		ItemStats stats = itemManager.getItemStats(itemId);
		return stats != null && stats.isEquipable();
	}

	public ItemStats getStats(int itemId)
	{
		return itemManager.getItemStats(itemId);
	}

	/**
	 * Returns a weapon subtype for items in the weapon slot.
	 * Non-weapon slots always return NONE.
	 */
	public WeaponSubtype getWeaponSubtype(int itemId, String name)
	{
		ItemStats stats = itemManager.getItemStats(itemId);
		if (stats == null || stats.getEquipment() == null) return WeaponSubtype.NONE;
		if (stats.getEquipment().getSlot() != WEAPON_SLOT) return WeaponSubtype.NONE;

		String lower = name.toLowerCase();

		if (lower.contains("crossbow"))                                          return WeaponSubtype.CROSSBOW;
		if (lower.contains("bow") && !lower.contains("elbow"))                  return WeaponSubtype.BOW;
		if (lower.contains("dart") || lower.contains("knife")
			|| lower.contains("javelin") || lower.contains("thrownaxe")
			|| lower.contains("chinchompa") || lower.contains("blowpipe")
			|| lower.contains("ballista"))                                       return WeaponSubtype.THROWN;
		if (lower.contains("staff") || lower.contains("wand")
			|| lower.contains("trident") || lower.contains("sceptre"))          return WeaponSubtype.STAFF;

		return WeaponSubtype.MELEE;
	}
}
