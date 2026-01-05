package com.justfatlard.usefulhoe.hoe;

/**
 * Defines area dimensions based on Reach enchantment level.
 * Width is perpendicular to player facing, depth is parallel.
 */
public enum HoeTier {
	REACH_0(1, 1),   // No enchantment
	REACH_1(1, 3),   // Reach I
	REACH_2(4, 4),   // Reach II
	REACH_3(4, 9),   // Reach III
	REACH_4(9, 9),   // Reach IV
	REACH_5(9, 18);  // Reach V

	public final int width;
	public final int depth;

	HoeTier(int width, int depth) {
		this.width = width;
		this.depth = depth;
	}

	/**
	 * Gets the tier for a given reach enchantment level.
	 */
	public static HoeTier fromReachLevel(int level) {
		int index = Math.min(Math.max(level, 0), values().length - 1);
		return values()[index];
	}
}
