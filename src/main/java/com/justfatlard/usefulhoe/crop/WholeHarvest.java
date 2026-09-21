package com.justfatlard.usefulhoe.crop;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Crops that are taken whole rather than picked and left standing.
 *
 * <p>A vanilla crop is harvested by breaking it and putting a seed back. Some modded crops are not
 * that shape: the block is a plant with a record behind it, breaking it is the harvest, and there
 * is nothing to replant because the seed comes out of the drops like everything else. Hemp is one -
 * its root block keeps the whole plant, and the stalks above it fall with it and drop nothing of
 * their own.
 *
 * <p>Which blocks these are is the block tag {@code #useful-hoe:harvested_whole}, so a mod joins in
 * by shipping a tag file and needs no dependency on this one, the same way
 * {@link RainGrowth#RAIN_GROWN} works.
 *
 * <p>When one is ready is asked of the block itself, through the one question vanilla already lets
 * any block answer about its own growth: whether bone meal would still do something for it. A crop
 * with nothing left to grow into is done, and a crop still coming on is not - a rule the owning mod
 * is already answering for its own bone meal, with all of its own reasons behind it. The cost of
 * borrowing that answer is that a mod cannot offer an earlier harvest: a plant it counts as still
 * growing, the hoe passes over.
 */
public final class WholeHarvest {

	private WholeHarvest() {}

	/** Crops the hoe takes whole. Anything may add to it; nothing has to. */
	public static final TagKey<Block> HARVESTED_WHOLE = TagKey.create(Registries.BLOCK,
		Identifier.fromNamespaceAndPath("useful-hoe", "harvested_whole"));

	/** Whether this is one of these crops and it has finished growing. */
	public static boolean isReady(Level world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);

		if (!state.is(HARVESTED_WHOLE)) {
			return false;
		}

		if (state.getBlock() instanceof BonemealableBlock growable) {
			return !growable.isValidBonemealTarget(world, pos, state, BonemealSource.INTERACTION);
		}

		// Nothing to ask: a tagged block that bone meal does not grow is taken as ready.
		return true;
	}
}
