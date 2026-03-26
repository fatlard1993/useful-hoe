package com.justfatlard.usefulhoe.crop;

import net.minecraft.block.*;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class CropHelper {

	private CropHelper() {}

	/** Includes generic AGE property fallback for modded crops. */
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

	public static boolean isCrop(BlockState state) {
		Block block = state.getBlock();
		return block instanceof CropBlock
			|| block instanceof NetherWartBlock
			|| block instanceof CocoaBlock
			|| block instanceof SweetBerryBushBlock;
	}

	public static boolean isSweetBerryBush(BlockState state) {
		return state.getBlock() instanceof SweetBerryBushBlock
			&& state.get(SweetBerryBushBlock.AGE) >= 2;
	}

	public static boolean isVerticalCrop(BlockState state) {
		Block block = state.getBlock();
		return block instanceof SugarCaneBlock
			|| block instanceof BambooBlock
			|| block instanceof CactusBlock
			|| block instanceof KelpPlantBlock;
	}

	/** Only harvestable if there's more of the same crop above the base. */
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
