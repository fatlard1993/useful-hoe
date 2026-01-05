package com.justfatlard.usefulhoe.action;

import com.justfatlard.usefulhoe.crop.CropHelper;
import com.justfatlard.usefulhoe.crop.SeedRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * Handles harvesting mature crops with auto-replanting.
 */
public final class HarvestAction {

	private HarvestAction() {}

	/**
	 * Checks if a position has a harvestable crop (either at the position or above it).
	 */
	public static boolean canHarvest(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);

		// Check for vertical crops (sugar cane, bamboo, cactus, kelp)
		if (CropHelper.canHarvestVertical(world, pos)) {
			return true;
		}

		// Check if clicked block itself is a mature crop
		if (CropHelper.isMatureCrop(state)) {
			return true;
		}

		// Check if block above is a mature crop (clicking on farmland)
		BlockPos above = pos.up();
		BlockState aboveState = world.getBlockState(above);
		return CropHelper.isMatureCrop(aboveState);
	}

	/**
	 * Harvests a mature crop and optionally replants.
	 * Handles crops, sweet berry bushes, and vertical crops.
	 * @param pos The clicked position
	 * @param offHand Seeds from player's off-hand for replanting
	 * @return true if harvesting was successful
	 */
	public static boolean execute(World world, BlockPos pos, PlayerEntity player, ItemStack offHand) {
		BlockState state = world.getBlockState(pos);

		// Handle vertical crops (sugar cane, bamboo, cactus, kelp)
		if (CropHelper.canHarvestVertical(world, pos)) {
			return harvestVertical(world, pos, player);
		}

		// Handle sweet berry bushes (pick without breaking)
		if (CropHelper.isSweetBerryBush(state)) {
			return harvestBerries(world, pos, player);
		}

		// Handle regular crops
		return harvestCrop(world, pos, player, offHand);
	}

	/**
	 * Harvests sweet berries without breaking the bush.
	 */
	private static boolean harvestBerries(World world, BlockPos pos, PlayerEntity player) {
		BlockState state = world.getBlockState(pos);
		int age = state.get(SweetBerryBushBlock.AGE);

		if (age < 2) return false;

		// Calculate berry drops (1-2 at age 2, 2-3 at age 3)
		int berryCount = 1 + world.random.nextInt(2);
		if (age == 3) berryCount++;

		// Drop berries
		Block.dropStack(world, pos, new ItemStack(Items.SWEET_BERRIES, berryCount));

		// Reset bush to age 1
		world.setBlockState(pos, state.with(SweetBerryBushBlock.AGE, 1));
		world.playSound(null, pos, SoundEvents.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, SoundCategory.BLOCKS, 1.0f, 1.0f);

		return true;
	}

	/**
	 * Harvests vertical crops (breaks all blocks above base).
	 */
	private static boolean harvestVertical(World world, BlockPos pos, PlayerEntity player) {
		// Start harvesting from one block above (keep the base)
		BlockPos harvestPos = pos.up();
		int harvested = 0;

		while (true) {
			BlockState state = world.getBlockState(harvestPos);
			if (!CropHelper.isVerticalCrop(state) && !(state.getBlock() instanceof net.minecraft.block.KelpBlock)) {
				break;
			}

			// Break and drop
			world.breakBlock(harvestPos, true, player);
			harvested++;
			harvestPos = harvestPos.up();
		}

		if (harvested > 0) {
			world.playSound(null, pos, SoundEvents.BLOCK_GRASS_BREAK, SoundCategory.BLOCKS, 1.0f, 1.0f);
		}

		return harvested > 0;
	}

	/**
	 * Harvests a regular crop with auto-replanting.
	 */
	private static boolean harvestCrop(World world, BlockPos pos, PlayerEntity player, ItemStack offHand) {
		// Determine actual crop position
		BlockPos cropPos;
		BlockState cropState = world.getBlockState(pos);

		if (CropHelper.isMatureCrop(cropState)) {
			cropPos = pos;
		} else {
			cropPos = pos.up();
			cropState = world.getBlockState(cropPos);
		}

		if (!CropHelper.isMatureCrop(cropState)) {
			return false;
		}

		Block cropBlock = cropState.getBlock();

		if (world instanceof ServerWorld serverWorld) {
			List<ItemStack> drops = Block.getDroppedStacks(
				cropState, serverWorld, cropPos, null, player, player.getMainHandStack()
			);

			// Find seed in drops for replanting
			Item seedItem = SeedRegistry.getSeedFor(cropBlock);
			ItemStack seedFromDrops = ItemStack.EMPTY;

			for (ItemStack drop : drops) {
				if (seedItem != null && drop.getItem() == seedItem && seedFromDrops.isEmpty()) {
					seedFromDrops = drop;
				} else {
					Block.dropStack(world, cropPos, drop);
				}
			}

			// Break the crop
			world.breakBlock(cropPos, false, player);

			// Replant
			if (cropBlock instanceof CropBlock) {
				Block offHandCrop = SeedRegistry.getCropFor(offHand.getItem());
				if (offHandCrop != null) {
					BlockState newCrop = offHandCrop.getDefaultState();
					world.setBlockState(cropPos, newCrop);
					if (!player.isCreative()) {
						offHand.decrement(1);
					}
					if (!seedFromDrops.isEmpty()) {
						Block.dropStack(world, cropPos, seedFromDrops);
					}
				} else if (!seedFromDrops.isEmpty()) {
					BlockState newCrop = cropBlock.getDefaultState();
					world.setBlockState(cropPos, newCrop);
					seedFromDrops.decrement(1);
					if (!seedFromDrops.isEmpty()) {
						Block.dropStack(world, cropPos, seedFromDrops);
					}
				}
			}
		}

		world.playSound(null, cropPos, SoundEvents.BLOCK_CROP_BREAK, SoundCategory.BLOCKS, 1.0f, 1.0f);
		return true;
	}
}
