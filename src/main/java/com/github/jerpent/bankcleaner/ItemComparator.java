package com.github.jerpent.bankcleaner;

import net.runelite.client.game.ItemStats;
import net.runelite.client.game.ItemEquipmentStats;

import java.util.ArrayList;
import java.util.List;

/**
 * Compares two equippable items by their combat stats.
 * Item A is considered redundant if item B is >= in every stat
 * and strictly > in at least one.
 *
 * Attack speed is included with inverted sign (lower tick = faster = better).
 * Weapon subtype must match for weapon-slot items.
 */
public class ItemComparator
{
	/**
	 * Returns true if `other` strictly dominates `item`.
	 * subtypeA and subtypeB must match for weapon-slot items.
	 */
	public boolean isDominatedBy(ItemStats item, ItemStats other,
		ItemClassifier.WeaponSubtype subtypeA, ItemClassifier.WeaponSubtype subtypeB)
	{
		if (item == null || other == null) return false;
		if (!item.isEquipable() || !other.isEquipable()) return false;

		ItemEquipmentStats a = item.getEquipment();
		ItemEquipmentStats b = other.getEquipment();

		if (a == null || b == null) return false;
		if (a.getSlot() != b.getSlot()) return false;

		// Weapons must be the same subtype to be comparable
		if (subtypeA != ItemClassifier.WeaponSubtype.NONE
			&& subtypeB != ItemClassifier.WeaponSubtype.NONE
			&& subtypeA != subtypeB)
		{
			return false;
		}

		boolean anyStrictlyBetter = false;

		int[] statsA = getStats(a);
		int[] statsB = getStats(b);

		for (int i = 0; i < statsA.length; i++)
		{
			if (statsB[i] < statsA[i]) return false;
			if (statsB[i] > statsA[i]) anyStrictlyBetter = true;
		}

		return anyStrictlyBetter;
	}

	/**
	 * Builds an HTML tooltip string showing only the stats where `other` beats `item`.
	 */
	public String buildStatComparison(ItemStats item, ItemStats other, String otherName)
	{
		if (item == null || other == null) return null;
		ItemEquipmentStats a = item.getEquipment();
		ItemEquipmentStats b = other.getEquipment();
		if (a == null || b == null) return null;

		String[] labels = {
			"Stab atk", "Slash atk", "Crush atk", "Magic atk", "Range atk",
			"Stab def", "Slash def", "Crush def", "Magic def", "Range def",
			"Strength", "Range str", "Magic dmg", "Prayer", "Speed"
		};
		int[] statsA = getStats(a);
		int[] statsB = getStats(b);

		List<String> diffs = new ArrayList<>();
		for (int i = 0; i < statsA.length; i++)
		{
			if (statsB[i] > statsA[i])
			{
				// Speed is stored negated — display the real tick values
				if (i == 14)
				{
					diffs.add(String.format("%s: %d vs %d ticks", labels[i], -statsA[i], -statsB[i]));
				}
				else
				{
					diffs.add(String.format("%s: %d vs %d", labels[i], statsA[i], statsB[i]));
				}
			}
		}

		if (diffs.isEmpty()) return otherName + " beats this item";

		StringBuilder sb = new StringBuilder("<html><b>" + otherName + " is better:</b><br>");
		for (String diff : diffs)
		{
			sb.append("&nbsp;").append(diff).append("<br>");
		}
		sb.append("</html>");
		return sb.toString();
	}

	/**
	 * Returns true if the item has at least one non-zero combat stat.
	 * Items with all zeros are assumed to be cosmetic and skipped.
	 */
	public boolean hasAnyStats(ItemStats stats)
	{
		if (stats == null || stats.getEquipment() == null) return false;
		ItemEquipmentStats s = stats.getEquipment();
		// Exclude aspeed from the check — all equippable items have a speed value
		return s.getAstab() != 0 || s.getAslash() != 0 || s.getAcrush() != 0
			|| s.getAmagic() != 0 || s.getArange() != 0
			|| s.getDstab() != 0 || s.getDslash() != 0 || s.getDcrush() != 0
			|| s.getDmagic() != 0 || s.getDrange() != 0
			|| s.getStr() != 0 || s.getRstr() != 0 || s.getMdmg() != 0 || s.getPrayer() != 0;
	}

	private int[] getStats(ItemEquipmentStats s)
	{
		return new int[]{
			s.getAstab(), s.getAslash(), s.getAcrush(), s.getAmagic(), s.getArange(),
			s.getDstab(), s.getDslash(), s.getDcrush(), s.getDmagic(), s.getDrange(),
			s.getStr(), s.getRstr(), (int) s.getMdmg(), s.getPrayer(),
			-s.getAspeed() // lower tick = faster = better, so negate for comparison
		};
	}
}
