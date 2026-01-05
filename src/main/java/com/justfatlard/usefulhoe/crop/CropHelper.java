package com.justfatlard.usefulhoe.crop;

import net.minecraft.block.*;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Utilities for detecting crop states.
 */
public final class CropHelper {

	private CropHelper() {}

	/**
	 * Checks if a block state represents a mature crop.
	 */
	public static boolean isMatureCrop(BlockState state) {
		Block block = state.getBlock();

		// Stems are NEVER harvestable - they produce fruit beside them
		if (block instanceof StemBlock) {
			return false;
		}

		// CropBlock (wheat, carrots, potatoes, beetroots)
		if (block instanceof CropBlock crop) {
			return crop.isMature(state);
		}

		// NetherWartBlock
		if (block instanceof NetherWartBlock) {
			return state.get(NetherWartBlock.AGE) >= 3;
		}

		// CocoaBlock
		if (block instanceof CocoaBlock) {
			return state.get(CocoaBlock.AGE) >= 2;
		}

		// SweetBerryBushBlock - harvestable at age 2+ (has berries)
		if (block instanceof SweetBerryBushBlock) {
			return state.get(SweetBerryBushBlock.AGE) >= 2;
		}

		// Generic age property check for modded crops
		if (state.contains(Properties.AGE_7)) {
			return state.get(Properties.AGE_7) >= 7;
		}
		if (state.contains(Properties.AGE_3)) {
			return state.get(Properties.AGE_3) >= 3;
		}
		if (state.contains(Properties.AGE_2)) {
			return state.get(Properties.AGE_2) >= 2;
		}

		return false;
	}

	/**
	 * Checks if a block is any type of crop (mature or not).
	 */
	public static boolean isCrop(BlockState state) {
		Block block = state.getBlock();
		return block instanceof CropBlock
			|| block instanceof NetherWartBlock
			|| block instanceof CocoaBlock
			|| block instanceof SweetBerryBushBlock;
	}

	/**
	 * Checks if a block is a sweet berry bush with berries.
	 */
	public static boolean isSweetBerryBush(BlockState state) {
		return state.getBlock() instanceof SweetBerryBushBlock
			&& state.get(SweetBerryBushBlock.AGE) >= 2;
	}

	/**
	 * Checks if a block is a vertical crop (sugar cane, bamboo, cactus, kelp).
	 */
	public static boolean isVerticalCrop(BlockState state) {
		Block block = state.getBlock();
		return block instanceof SugarCaneBlock
			|| block instanceof BambooBlock
			|| block instanceof CactusBlock
			|| block instanceof KelpPlantBlock;
	}

	/**
	 * Checks if a block is harvestable as a vertical crop (has blocks above).
	 */
	public static boolean canHarvestVertical(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if (!isVerticalCrop(state)) {
			return false;
		}
		// Only harvest if there's more of the same crop above
		BlockState above = world.getBlockState(pos.up());
		return isVerticalCrop(above) || above.getBlock() instanceof KelpBlock;
	}
}
