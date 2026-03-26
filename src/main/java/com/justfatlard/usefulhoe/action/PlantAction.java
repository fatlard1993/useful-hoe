package com.justfatlard.usefulhoe.action;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class PlantAction {

	private PlantAction() {}

	/** Handles clicking on farmland directly or on air above farmland. */
	public static boolean canPlant(World world, BlockPos pos, BlockState state) {
		// Check if clicked position is farmland/soul sand with empty space above
		if (state.isOf(Blocks.FARMLAND) || state.isOf(Blocks.SOUL_SAND)) {
			BlockState above = world.getBlockState(pos.up());
			return above.isAir();
		}

		// Check if clicked on air/crop - look for farmland below
		BlockPos below = pos.down();
		BlockState belowState = world.getBlockState(below);
		if (belowState.isOf(Blocks.FARMLAND) || belowState.isOf(Blocks.SOUL_SAND)) {
			return state.isAir(); // Position must be empty for planting
		}

		return false;
	}

	public static BlockPos getFarmlandPos(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if (state.isOf(Blocks.FARMLAND) || state.isOf(Blocks.SOUL_SAND)) {
			return pos;
		}
		return pos.down();
	}

	public static boolean execute(World world, BlockPos pos, PlayerEntity player, ItemStack seedStack) {
		if (seedStack.isEmpty()) {
			return false;
		}

		BlockState state = world.getBlockState(pos);
		if (!canPlant(world, pos, state)) {
			return false;
		}

		// Determine actual farmland position
		BlockPos farmlandPos = getFarmlandPos(world, pos);
		BlockState groundState = world.getBlockState(farmlandPos);

		// Check if seed item can be planted
		if (!(seedStack.getItem() instanceof BlockItem blockItem)) {
			return false;
		}

		Block seedBlock = blockItem.getBlock();

		// Verify the crop can be planted on this ground
		if (seedBlock instanceof CropBlock) {
			if (!groundState.isOf(Blocks.FARMLAND)) {
				return false;
			}
		} else if (seedBlock == Blocks.NETHER_WART) {
			if (!groundState.isOf(Blocks.SOUL_SAND)) {
				return false;
			}
		} else {
			// Unknown seed type
			return false;
		}

		BlockPos plantPos = farmlandPos.up();
		BlockState cropState = seedBlock.getDefaultState();

		world.setBlockState(plantPos, cropState);
		world.playSound(null, plantPos, SoundEvents.ITEM_CROP_PLANT, SoundCategory.BLOCKS, 1.0f, 1.0f);

		// Consume seed
		if (!player.isCreative()) {
			seedStack.decrement(1);
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
