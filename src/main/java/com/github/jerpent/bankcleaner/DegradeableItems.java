package com.github.jerpent.bankcleaner;

import java.util.Set;

/**
 * Hardcoded set of item IDs for known degradeable equipment.
 * Covers items not detectable by name-pattern alone (non-Barrows).
 */
public class DegradeableItems
{
	public static final Set<Integer> IDS = Set.of(
		// Abyssal tentacle
		12006,
		// Scythe of Vitur (charged/uncharged)
		22325, 22324,
		// Sanguinesti staff (charged/uncharged)
		22481, 22480,
		// Tumeken's shadow (charged/uncharged)
		27275, 27277,
		// Venator bow
		27610,
		// Blade of Saeldor (charged/uncharged/corrupted)
		25867, 25869, 25870,
		// Bow of Faerdhinen (charged/uncharged/corrupted)
		25862, 25864, 25865,
		// Crystal equipment (bow, halberd, shield)
		23971, 23973, 23975,
		// Serpentine helm (charged/uncharged)
		12931, 13200,
		// Trident of the seas / swamp
		11905, 12899,
		// Toxic blowpipe
		12926,
		// Toxic staff of the dead
		12904,
		// Imbued heart
		20724
	);

	/**
	 * Returns true if the item name matches a Barrows degradation state
	 * e.g. "Dharok's greataxe 0", "Guthan's platebody 75"
	 */
	public static boolean isBarrowsVariant(String name)
	{
		return name.matches(".+\\s(0|25|50|75)$");
	}
}
