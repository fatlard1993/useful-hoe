package com.justfatlard.usefulhoe.action;

import com.justfatlard.usefulhoe.hoe.HoeAreaCalculator;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * Main handler for hoe interactions.
 * Orchestrates area-based tilling, planting, and harvesting.
 */
public final class HoeActionHandler {

	private HoeActionHandler() {}

	/**
	 * Handles hoe use on a block.
	 * Called from UseBlockCallback.
	 * Performs cascading actions: till -> plant -> harvest
	 */
	public static ActionResult handleUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
		ItemStack mainHand = player.getStackInHand(Hand.MAIN_HAND);

		// Only process if holding a hoe in main hand
		if (!(mainHand.getItem() instanceof HoeItem)) {
			return ActionResult.PASS;
		}

		// Skip if sneaking (allow vanilla behavior)
		if (player.isSneaking()) {
			return ActionResult.PASS;
		}

		BlockPos targetPos = hitResult.getBlockPos();
		BlockState targetState = world.getBlockState(targetPos);

		// Only intercept if the clicked block is hoe-relevant (tillable, farmland, crop, etc.)
		// This allows interactions with chests, furnaces, etc. even when near farmland
		if (!isHoeRelevantBlock(world, targetPos, targetState)) {
			return ActionResult.PASS;
		}

		// Client: return success to indicate we handle this
		if (world.isClient()) {
			// Check if any action is possible in the area
			if (hasAnyAction(world, targetPos, player, mainHand)) {
				return ActionResult.SUCCESS;
			}
			return ActionResult.PASS;
		}

		// Server: perform cascading actions on all affected blocks
		List<BlockPos> affectedPositions = HoeAreaCalculator.calculateArea(
			targetPos, player, mainHand, world
		);

		ItemStack offHand = player.getOffHandStack();
		int affected = executeCascadingActions(player, world, affectedPositions, offHand);

		if (affected > 0) {
			// Durability cost: 1 base + 1 per affected block (balances powerful area effects)
			int durabilityCost = 1 + affected;
			mainHand.damage(durabilityCost, player, EquipmentSlot.MAINHAND);
			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}

	/**
	 * Checks if any action can be performed in the area.
	 */
	private static boolean hasAnyAction(World world, BlockPos targetPos, PlayerEntity player, ItemStack mainHand) {
		List<BlockPos> positions = HoeAreaCalculator.calculateArea(targetPos, player, mainHand, world);
		ItemStack offHand = player.getOffHandStack();

		for (BlockPos pos : positions) {
			BlockState state = world.getBlockState(pos);
			if (TillAction.canTill(world, pos, state)) return true;
			if (PlantAction.isPlantableSeed(offHand) && PlantAction.canPlant(world, pos, state)) return true;
			if (BonemealAction.isBonemeal(offHand) && BonemealAction.canBonemealCrop(world, pos)) return true;
			if (HarvestAction.canHarvest(world, pos)) return true;
		}
		return false;
	}

	/**
	 * Executes ONE action stage in priority order: till -> plant -> bonemeal -> harvest
	 * One click = one stage. Processes all blocks for that stage, then stops.
	 * If a stage has no valid targets, moves to the next stage.
	 */
	private static int executeCascadingActions(
			PlayerEntity player,
			World world,
			List<BlockPos> positions,
			ItemStack offHand) {

		int successCount = 0;

		// Stage 1: Till all tillable blocks
		for (BlockPos pos : positions) {
			BlockState state = world.getBlockState(pos);
			if (TillAction.canTill(world, pos, state)) {
				if (TillAction.execute(world, pos, player)) {
					successCount++;
				}
			}
		}
		if (successCount > 0) return successCount;

		// Stage 2: Plant on all plantable blocks (if holding seeds)
		if (PlantAction.isPlantableSeed(offHand)) {
			for (BlockPos pos : positions) {
				BlockState state = world.getBlockState(pos);
				if (PlantAction.canPlant(world, pos, state)) {
					if (PlantAction.execute(world, pos, player, offHand)) {
						successCount++;
					}
				}
			}
			if (successCount > 0) return successCount;
		}

		// Stage 3: Bonemeal all fertilizable crops (if holding bone meal)
		if (BonemealAction.isBonemeal(offHand)) {
			for (BlockPos pos : positions) {
				if (BonemealAction.canBonemealCrop(world, pos)) {
					if (BonemealAction.execute(world, pos, player, offHand)) {
						successCount++;
					}
				}
			}
			if (successCount > 0) return successCount;
		}

		// Stage 4: Harvest all harvestable crops
		for (BlockPos pos : positions) {
			if (HarvestAction.canHarvest(world, pos)) {
				if (HarvestAction.execute(world, pos, player, offHand)) {
					successCount++;
				}
			}
		}

		return successCount;
	}

	/**
	 * Determines the action type for preview rendering (client-side).
	 */
	public static HoeAction getActionForPreview(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);

		if (HarvestAction.canHarvest(world, pos)) {
			return HoeAction.HARVEST;
		}
		if (TillAction.canTill(world, pos, state)) {
			return HoeAction.TILL;
		}
		if (PlantAction.canPlant(world, pos, state)) {
			return HoeAction.PLANT;
		}

		return null;
	}

	/**
	 * Checks if the clicked block is something the hoe should handle.
	 * Returns false for non-farming blocks (chests, furnaces, etc.)
	 * to allow vanilla interactions.
	 */
	private static boolean isHoeRelevantBlock(World world, BlockPos pos, BlockState state) {
		// Tillable blocks (dirt, grass, etc.)
		if (TillAction.canTill(world, pos, state)) {
			return true;
		}

		// Farmland or soul sand (planting targets)
		if (PlantAction.canPlant(world, pos, state)) {
			return true;
		}

		// Harvestable crops
		if (HarvestAction.canHarvest(world, pos)) {
			return true;
		}

		// Bonemealable crops
		if (BonemealAction.canBonemealCrop(world, pos)) {
			return true;
		}

		return false;
	}
}
