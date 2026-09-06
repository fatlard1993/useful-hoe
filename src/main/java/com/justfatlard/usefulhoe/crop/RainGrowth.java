package com.justfatlard.usefulhoe.crop;

import com.justfatlard.usefulhoe.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Rain on an open field brings the crop on faster.
 *
 * <p>A crop under the sky in the rain gets extra chances to grow, on exactly the terms it already
 * grows by. The extra chance is another random tick rather than a bonus applied on top, so
 * everything vanilla weighs - the light level, the farmland's moisture, how well spaced the rows
 * are - still decides whether it comes to anything. Rain makes the roll come round more often; it
 * does not make a crop in the dark grow.
 *
 * <p>What counts as a crop is the block tag {@code #useful-hoe:rain_grown}, not a list of classes.
 * A mod adds its own by shipping a tag file and needs no dependency on this one - which is the only
 * way it works out, because a mod's crops rarely all share an ancestor: dirt-slab's slab crops
 * extend {@code CropBlock}, its slab stems extend {@code VegetationBlock}, and its sugar cane
 * extends {@code Block}.
 */
public final class RainGrowth {
	private RainGrowth() {}

	/** Blocks that rain brings on. Anything may add to it; nothing has to. */
	public static final TagKey<Block> RAIN_GROWN = TagKey.create(Registries.BLOCK,
		Identifier.fromNamespaceAndPath("useful-hoe", "rain_grown"));

	/**
	 * Guards against the extra tick asking for an extra tick of its own.
	 *
	 * <p>The bonus is delivered by running the block's own random tick again, which lands straight
	 * back in the injection that called it. Per-thread because more than one world ticks at once.
	 */
	private static final ThreadLocal<Boolean> GRANTING = ThreadLocal.withInitial(() -> false);

	/**
	 * Called after any block's random tick.
	 *
	 * <p>Every random tick in the game comes through here, so the questions are asked cheapest
	 * first: whether it is raining anywhere in this world is a field read and rules out nearly
	 * every call, the tag rules out nearly all of the rest, and only then is it worth working out
	 * whether this particular spot can see the sky.
	 */
	public static void afterRandomTick(BlockState state, ServerLevel level, BlockPos pos,
			RandomSource random) {
		if (!level.isRaining()) return;
		if (GRANTING.get()) return;
		if (!state.is(RAIN_GROWN)) return;

		ModConfig config = ModConfig.get();
		if (!config.rainGrowthEnabled || config.rainGrowthBonusTicks <= 0) return;

		// Answers the rest at once: this spot can see the sky, and the weather here falls as rain
		// rather than snow. A crop under a roof gets nothing.
		if (!level.isRainingAt(pos)) return;

		GRANTING.set(true);
		try {
			for (int i = 0; i < config.rainGrowthBonusTicks; i++) {
				// Re-read each time: the crop may have grown, and a grown crop is a different state.
				// Stop if it is no longer the same block - something else has taken the position and
				// it is not ours to keep growing.
				BlockState current = level.getBlockState(pos);
				if (!current.is(state.getBlock())) return;

				current.randomTick(level, pos, random);
			}
		} finally {
			GRANTING.set(false);
		}
	}
}
