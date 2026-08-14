package com.justfatlard.usefulhoe.action;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class PlantAction {

	private PlantAction() {}

	/** Handles clicking on farmland directly or on air above farmland. */
	public static boolean canPlant(Level world, BlockPos pos, BlockState state) {
		if (state.getBlock() == Blocks.FARMLAND || state.getBlock() == Blocks.SOUL_SAND) {
			BlockState above = world.getBlockState(pos.above());
			return above.isAir();
		}

		BlockPos below = pos.below();
		BlockState belowState = world.getBlockState(below);
		if (belowState.getBlock() == Blocks.FARMLAND || belowState.getBlock() == Blocks.SOUL_SAND) {
			return state.isAir();
		}

		return false;
	}

	public static BlockPos getFarmlandPos(Level world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if (state.getBlock() == Blocks.FARMLAND || state.getBlock() == Blocks.SOUL_SAND) {
			return pos;
		}
		return pos.below();
	}

	public static boolean execute(Level world, BlockPos pos, Player player, ItemStack seedStack) {
		if (seedStack.isEmpty()) {
			return false;
		}

		BlockState state = world.getBlockState(pos);
		if (!canPlant(world, pos, state)) {
			return false;
		}

		BlockPos farmlandPos = getFarmlandPos(world, pos);
		BlockState groundState = world.getBlockState(farmlandPos);

		if (!(seedStack.getItem() instanceof BlockItem blockItem)) {
			return false;
		}

		Block seedBlock = blockItem.getBlock();

		if (seedBlock instanceof CropBlock) {
			if (groundState.getBlock() != Blocks.FARMLAND) {
				return false;
			}
		} else if (seedBlock == Blocks.NETHER_WART) {
			if (groundState.getBlock() != Blocks.SOUL_SAND) {
				return false;
			}
		} else {
			return false;
		}

		BlockPos plantPos = farmlandPos.above();
		BlockState cropState = seedBlock.defaultBlockState();

		world.setBlockAndUpdate(plantPos, cropState);
		world.playSound(null, plantPos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0f, 1.0f);

		if (!player.isCreative()) {
			seedStack.shrink(1);
		}

		return true;
	}

	public static boolean isPlantableSeed(ItemStack stack) {
		if (stack.isEmpty()) return false;
		if (!(stack.getItem() instanceof BlockItem blockItem)) return false;

		Block block = blockItem.getBlock();
		return block instanceof CropBlock || block == Blocks.NETHER_WART;
	}
}
