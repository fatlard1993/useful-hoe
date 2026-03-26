package com.justfatlard.usefulhoe.action;

import com.justfatlard.usefulhoe.config.ModConfig;
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
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Orchestrates area-based hoe actions with cascading priority.
 */
public final class HoeActionHandler {

	/** Minimum ticks between area actions per player (prevents spam/exploit). */
	private static final int COOLDOWN_TICKS = 4;
	private static final Map<PlayerEntity, Long> lastActionTick = new WeakHashMap<>();

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

		// Skip if sneaking (allow vanilla behavior) or spectator
		if (player.isSneaking() || player.isSpectator()) {
			return ActionResult.PASS;
		}

		BlockPos targetPos = hitResult.getBlockPos();
		BlockState targetState = world.getBlockState(targetPos);

		// Only intercept if the clicked block is hoe-relevant
		if (!isHoeRelevantBlock(world, targetPos, targetState)) {
			return ActionResult.PASS;
		}

		// Client: return success to indicate we handle this
		if (world.isClient()) {
			if (hasAnyAction(world, targetPos, player, mainHand)) {
				return ActionResult.SUCCESS;
			}
			return ActionResult.PASS;
		}

		// Rate limit to prevent spam/exploit
		long currentTick = world.getTime();
		Long lastTick = lastActionTick.get(player);
		if (lastTick != null && currentTick - lastTick < COOLDOWN_TICKS) {
			return ActionResult.PASS;
		}
		lastActionTick.put(player, currentTick);

		// Server: perform cascading actions on all affected blocks
		List<BlockPos> affectedPositions = HoeAreaCalculator.calculateArea(
			targetPos, player, mainHand, world
		);

		ItemStack offHand = player.getOffHandStack();
		int affected = executeCascadingActions(player, world, affectedPositions, offHand);

		if (affected > 0) {
			if (!player.isCreative()) {
				ModConfig config = ModConfig.get();
				int durabilityCost = config.durabilityBaseCost + config.durabilityPerBlock * affected;
				mainHand.damage(durabilityCost, player, EquipmentSlot.MAINHAND);
			}
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
			if (getFirstApplicableAction(world, pos, offHand) != null) return true;
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

		ModConfig config = ModConfig.get();
		int successCount = 0;

		// Stage 1: Till all tillable blocks
		if (config.tillEnabled) {
			for (BlockPos pos : positions) {
				BlockState state = world.getBlockState(pos);
				if (TillAction.canTill(world, pos, state)) {
					if (TillAction.execute(world, pos, player)) {
						successCount++;
					}
				}
			}
			if (successCount > 0) return successCount;
		}

		// Stage 2: Plant on all plantable blocks (if holding seeds)
		if (config.plantEnabled && PlantAction.isPlantableSeed(offHand)) {
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
		if (config.bonemealEnabled && BonemealAction.isBonemeal(offHand)) {
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
		if (config.harvestEnabled) {
			for (BlockPos pos : positions) {
				if (HarvestAction.canHarvest(world, pos)) {
					if (HarvestAction.execute(world, pos, player, offHand)) {
						successCount++;
					}
				}
			}
		}

		return successCount;
	}

	/**
	 * Checks if the clicked block is something the hoe should handle.
	 * Returns false for non-farming blocks to allow vanilla interactions.
	 */
	public static boolean isHoeRelevantBlock(World world, BlockPos pos, BlockState state) {
		return TillAction.canTill(world, pos, state)
			|| PlantAction.canPlant(world, pos, state)
			|| HarvestAction.canHarvest(world, pos)
			|| BonemealAction.canBonemealCrop(world, pos);
	}

	/**
	 * Gets the first applicable action for a position, respecting priority order.
	 * Used by both execution (to check feasibility) and preview rendering.
	 */
	public static HoeAction getFirstApplicableAction(World world, BlockPos pos, ItemStack offHand) {
		BlockState state = world.getBlockState(pos);

		if (TillAction.canTill(world, pos, state)) {
			return HoeAction.TILL;
		}
		if (PlantAction.isPlantableSeed(offHand) && PlantAction.canPlant(world, pos, state)) {
			return HoeAction.PLANT;
		}
		if (BonemealAction.isBonemeal(offHand) && BonemealAction.canBonemealCrop(world, pos)) {
			return HoeAction.BONEMEAL;
		}
		if (HarvestAction.canHarvest(world, pos)) {
			return HoeAction.HARVEST;
		}
		return null;
	}
}
