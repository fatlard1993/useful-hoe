package com.justfatlard.usefulhoe.action;

import com.justfatlard.usefulhoe.crop.CropHelper;
import com.justfatlard.usefulhoe.crop.SeedRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;

public final class HarvestAction {

	private static final int DESTROY_UPDATE_LIMIT = 512;

	private HarvestAction() {}

	/** Checks pos and pos.above() for mature crops, plus vertical crops. */
	public static boolean canHarvest(Level world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);

		if (CropHelper.canHarvestVertical(world, pos)) {
			return true;
		}

		if (CropHelper.isMatureCrop(state)) {
			return true;
		}

		BlockPos above = pos.above();
		BlockState aboveState = world.getBlockState(above);
		return CropHelper.isMatureCrop(aboveState);
	}

	public static boolean execute(Level world, BlockPos pos, Player player, ItemStack offHand) {
		BlockState state = world.getBlockState(pos);

		if (CropHelper.canHarvestVertical(world, pos)) {
			return harvestVertical(world, pos, player);
		}

		if (CropHelper.isSweetBerryBush(state)) {
			return harvestBerries(world, pos, player);
		}

		return harvestCrop(world, pos, player, offHand);
	}

	private static boolean harvestBerries(Level world, BlockPos pos, Player player) {
		BlockState state = world.getBlockState(pos);
		int age = state.getValue(SweetBerryBushBlock.AGE);

		if (age < 2) return false;

		// Vanilla drop rates: 1-2 at age 2, 2-3 at age 3
		int berryCount = 1 + world.getRandom().nextInt(2);
		if (age == 3) berryCount++;

		Block.popResource(world, pos, new ItemStack(Items.SWEET_BERRIES, berryCount));

		world.setBlockAndUpdate(pos, state.setValue(SweetBerryBushBlock.AGE, 1));
		world.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0f, 1.0f);

		return true;
	}

	/** Breaks all blocks above base, keeps the base block. */
	private static boolean harvestVertical(Level world, BlockPos pos, Player player) {
		BlockPos harvestPos = pos.above();
		int harvested = 0;

		while (true) {
			BlockState state = world.getBlockState(harvestPos);
			if (!CropHelper.isVerticalCrop(state) && !(state.getBlock() instanceof net.minecraft.world.level.block.KelpBlock)) {
				break;
			}

			world.destroyBlock(harvestPos, true, player, DESTROY_UPDATE_LIMIT);
			harvested++;
			harvestPos = harvestPos.above();
		}

		if (harvested > 0) {
			world.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
		}

		return harvested > 0;
	}

	/** Replants using off-hand seeds if available, otherwise uses a seed from drops. */
	private static boolean harvestCrop(Level world, BlockPos pos, Player player, ItemStack offHand) {
		BlockPos cropPos;
		BlockState cropState = world.getBlockState(pos);

		if (CropHelper.isMatureCrop(cropState)) {
			cropPos = pos;
		} else {
			cropPos = pos.above();
			cropState = world.getBlockState(cropPos);
		}

		if (!CropHelper.isMatureCrop(cropState)) {
			return false;
		}

		Block cropBlock = cropState.getBlock();

		if (world instanceof ServerLevel serverWorld) {
			List<ItemStack> drops = Block.getDrops(
				cropState, serverWorld, cropPos, null, player, player.getMainHandItem()
			);

			// Withhold one seed from the drops for replanting
			Item seedItem = SeedRegistry.getSeedFor(cropBlock);
			ItemStack seedFromDrops = ItemStack.EMPTY;

			for (ItemStack drop : drops) {
				if (seedItem != null && drop.getItem() == seedItem && seedFromDrops.isEmpty()) {
					seedFromDrops = drop;
				} else {
					Block.popResource(world, cropPos, drop);
				}
			}

			world.destroyBlock(cropPos, false, player, DESTROY_UPDATE_LIMIT);

			// Replant
			if (cropBlock instanceof CropBlock) {
				Block offHandCrop = SeedRegistry.getCropFor(offHand.getItem());
				if (offHandCrop != null) {
					BlockState newCrop = offHandCrop.defaultBlockState();
					world.setBlockAndUpdate(cropPos, newCrop);
					if (!player.isCreative()) {
						offHand.shrink(1);
					}
					if (!seedFromDrops.isEmpty()) {
						Block.popResource(world, cropPos, seedFromDrops);
					}
				} else if (!seedFromDrops.isEmpty()) {
					BlockState newCrop = cropBlock.defaultBlockState();
					world.setBlockAndUpdate(cropPos, newCrop);
					seedFromDrops.shrink(1);
					if (!seedFromDrops.isEmpty()) {
						Block.popResource(world, cropPos, seedFromDrops);
					}
				}
			}
		}

		world.playSound(null, cropPos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
		return true;
	}
}
