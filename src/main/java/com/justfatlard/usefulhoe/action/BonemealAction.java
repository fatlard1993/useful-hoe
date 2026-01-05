package com.justfatlard.usefulhoe.action;

import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Handles applying bone meal to fertilizable blocks.
 */
public final class BonemealAction {

	private BonemealAction() {}

	/**
	 * Checks if bone meal can be applied at this position.
	 */
	public static boolean canBonemeal(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);

		if (!(state.getBlock() instanceof Fertilizable fertilizable)) {
			return false;
		}

		// Check if the block can grow with bone meal
		return fertilizable.isFertilizable(world, pos, state);
	}

	/**
	 * Applies bone meal to a block.
	 * Handles both clicking on the crop directly or on farmland below.
	 * @param pos The clicked position
	 * @param bonemealStack The bone meal item stack from off-hand
	 * @return true if bone meal was applied
	 */
	public static boolean execute(World world, BlockPos pos, PlayerEntity player, ItemStack bonemealStack) {
		if (bonemealStack.isEmpty() || !isBonemeal(bonemealStack)) {
			return false;
		}

		// Determine actual crop position - check clicked block first, then above
		BlockPos cropPos;
		BlockState cropState = world.getBlockState(pos);

		if (cropState.getBlock() instanceof Fertilizable) {
			cropPos = pos;
		} else {
			cropPos = pos.up();
			cropState = world.getBlockState(cropPos);
		}

		if (!(cropState.getBlock() instanceof Fertilizable fertilizable)) {
			return false;
		}

		if (!fertilizable.isFertilizable(world, cropPos, cropState)) {
			return false;
		}

		if (world instanceof ServerWorld serverWorld) {
			if (fertilizable.canGrow(world, world.random, cropPos, cropState)) {
				fertilizable.grow(serverWorld, world.random, cropPos, cropState);

				// Consume bone meal
				if (!player.isCreative()) {
					bonemealStack.decrement(1);
				}

				// Spawn particles and play sound
				world.playSound(null, cropPos, SoundEvents.ITEM_BONE_MEAL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
				BoneMealItem.createParticles(serverWorld, cropPos, 0);

				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if an item stack is bone meal.
	 */
	public static boolean isBonemeal(ItemStack stack) {
		return !stack.isEmpty() && stack.isOf(Items.BONE_MEAL);
	}

	/**
	 * Checks if bone meal can be applied at this position or the crop above.
	 */
	public static boolean canBonemealCrop(World world, BlockPos pos) {
		// Check clicked block first
		if (canBonemeal(world, pos)) {
			return true;
		}
		// Check block above (if clicked on farmland)
		return canBonemeal(world, pos.up());
	}
}
