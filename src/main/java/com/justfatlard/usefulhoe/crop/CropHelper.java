package com.justfatlard.usefulhoe.crop;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class CropHelper {

	private CropHelper() {}

	/** Includes generic AGE property fallback for modded crops. */
	public static boolean isMatureCrop(BlockState state) {
		Block block = state.getBlock();

		// Stems are NEVER harvestable - they produce fruit beside them
		if (block instanceof StemBlock) {
			return false;
		}

		if (block instanceof CropBlock crop) {
			return crop.isMaxAge(state);
		}

		if (block instanceof NetherWartBlock) {
			return state.getValue(NetherWartBlock.AGE) >= 3;
		}

		if (block instanceof CocoaBlock) {
			return state.getValue(CocoaBlock.AGE) >= 2;
		}

		// age >= 2 means the bush has berries
		if (block instanceof SweetBerryBushBlock) {
			return state.getValue(SweetBerryBushBlock.AGE) >= 2;
		}

		if (state.hasProperty(BlockStateProperties.AGE_7)) {
			return state.getValue(BlockStateProperties.AGE_7) >= 7;
		}
		if (state.hasProperty(BlockStateProperties.AGE_3)) {
			return state.getValue(BlockStateProperties.AGE_3) >= 3;
		}
		if (state.hasProperty(BlockStateProperties.AGE_2)) {
			return state.getValue(BlockStateProperties.AGE_2) >= 2;
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
			&& state.getValue(SweetBerryBushBlock.AGE) >= 2;
	}

	public static boolean isVerticalCrop(BlockState state) {
		Block block = state.getBlock();
		return block instanceof SugarCaneBlock
			|| block instanceof BambooStalkBlock
			|| block instanceof CactusBlock
			|| block instanceof KelpPlantBlock;
	}

	/** Only harvestable if there's more of the same crop above the base. */
	public static boolean canHarvestVertical(Level world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if (!isVerticalCrop(state)) {
			return false;
		}
		BlockState above = world.getBlockState(pos.above());
		return isVerticalCrop(above) || above.getBlock() instanceof KelpBlock;
	}
}
