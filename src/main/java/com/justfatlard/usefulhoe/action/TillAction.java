package com.justfatlard.usefulhoe.action;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

/**
 * Handles tilling dirt/grass into farmland.
 */
public final class TillAction {

	private static final Map<Block, BlockState> TILLABLE = Map.of(
		Blocks.GRASS_BLOCK, Blocks.FARMLAND.getDefaultState(),
		Blocks.DIRT, Blocks.FARMLAND.getDefaultState(),
		Blocks.DIRT_PATH, Blocks.FARMLAND.getDefaultState(),
		Blocks.COARSE_DIRT, Blocks.DIRT.getDefaultState(),
		Blocks.ROOTED_DIRT, Blocks.DIRT.getDefaultState()
	);

	private TillAction() {}

	/**
	 * Checks if a block can be tilled.
	 */
	public static boolean canTill(World world, BlockPos pos, BlockState state) {
		if (!TILLABLE.containsKey(state.getBlock())) {
			return false;
		}

		// Check if block above is air or replaceable
		BlockState above = world.getBlockState(pos.up());
		return above.isAir() || above.isReplaceable();
	}

	/**
	 * Tills a block at the given position.
	 * @return true if tilling was successful
	 */
	public static boolean execute(World world, BlockPos pos, PlayerEntity player) {
		BlockState state = world.getBlockState(pos);

		if (!canTill(world, pos, state)) {
			return false;
		}

		BlockState tilled = TILLABLE.get(state.getBlock());
		if (tilled == null) {
			return false;
		}

		world.setBlockState(pos, tilled);
		world.playSound(null, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0f, 1.0f);

		return true;
	}
}
