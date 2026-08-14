package com.justfatlard.usefulhoe.action;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class BonemealAction {

	private BonemealAction() {}

	/** Checks the exact position only. See canBonemealCrop() for pos + pos.above(). */
	public static boolean canBonemeal(Level world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);

		if (!(state.getBlock() instanceof BonemealableBlock fertilizable)) {
			return false;
		}

		return fertilizable.isValidBonemealTarget(world, pos, state, BonemealSource.INTERACTION);
	}

	public static boolean execute(Level world, BlockPos pos, Player player, ItemStack bonemealStack) {
		if (bonemealStack.isEmpty() || !isBonemeal(bonemealStack)) {
			return false;
		}

		BlockPos cropPos;
		BlockState cropState = world.getBlockState(pos);

		if (cropState.getBlock() instanceof BonemealableBlock) {
			cropPos = pos;
		} else {
			cropPos = pos.above();
			cropState = world.getBlockState(cropPos);
		}

		if (!(cropState.getBlock() instanceof BonemealableBlock fertilizable)) {
			return false;
		}

		if (!fertilizable.isValidBonemealTarget(world, cropPos, cropState, BonemealSource.INTERACTION)) {
			return false;
		}

		if (world instanceof ServerLevel serverWorld) {
			if (fertilizable.isBonemealSuccess(world, world.getRandom(), cropPos, cropState, BonemealSource.INTERACTION)) {
				fertilizable.performBonemeal(serverWorld, world.getRandom(), cropPos, cropState, BonemealSource.INTERACTION);

				if (!player.isCreative()) {
					bonemealStack.shrink(1);
				}

				world.playSound(null, cropPos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
				BoneMealItem.addGrowthParticles(serverWorld, cropPos, 0);

				return true;
			}
		}

		return false;
	}

	public static boolean isBonemeal(ItemStack stack) {
		return !stack.isEmpty() && stack.is(Items.BONE_MEAL);
	}

	/** Checks both pos and pos.above() for fertilizable blocks. */
	public static boolean canBonemealCrop(Level world, BlockPos pos) {
		if (canBonemeal(world, pos)) {
			return true;
		}
		return canBonemeal(world, pos.above());
	}
}
