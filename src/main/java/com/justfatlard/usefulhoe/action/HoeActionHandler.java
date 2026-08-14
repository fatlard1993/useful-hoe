package com.justfatlard.usefulhoe.action;

import com.justfatlard.usefulhoe.config.ModConfig;
import com.justfatlard.usefulhoe.hoe.HoeAreaCalculator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Orchestrates area-based hoe actions with cascading priority.
 */
public final class HoeActionHandler {

	private static final int COOLDOWN_TICKS = 4;
	private static final Map<Player, Long> lastActionTick = new WeakHashMap<>();

	private HoeActionHandler() {}

	public static InteractionResult handleUseBlock(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
		ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);

		if (!mainHand.is(ItemTags.HOES)) {
			return InteractionResult.PASS;
		}

		// Sneak bypasses to vanilla single-block behavior
		if (player.isShiftKeyDown() || player.isSpectator()) {
			return InteractionResult.PASS;
		}

		BlockPos targetPos = hitResult.getBlockPos();
		BlockState targetState = world.getBlockState(targetPos);

		if (!isHoeRelevantBlock(world, targetPos, targetState)) {
			return InteractionResult.PASS;
		}

		if (world.isClientSide()) {
			if (hasAnyAction(world, targetPos, player, mainHand)) {
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		}

		// Rate limit to prevent spam/exploit
		long currentTick = world.getGameTime();
		Long lastTick = lastActionTick.get(player);
		if (lastTick != null && currentTick - lastTick < COOLDOWN_TICKS) {
			return InteractionResult.PASS;
		}
		lastActionTick.put(player, currentTick);

		List<BlockPos> affectedPositions = HoeAreaCalculator.calculateArea(
			targetPos, player, mainHand, world
		);

		ItemStack offHand = player.getOffhandItem();
		int affected = executeCascadingActions(player, world, affectedPositions, offHand);

		if (affected > 0) {
			if (!player.isCreative()) {
				ModConfig config = ModConfig.get();
				int durabilityCost = config.durabilityBaseCost + config.durabilityPerBlock * affected;
				mainHand.hurtAndBreak(durabilityCost, player, EquipmentSlot.MAINHAND);
			}
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	private static boolean hasAnyAction(Level world, BlockPos targetPos, Player player, ItemStack mainHand) {
		List<BlockPos> positions = HoeAreaCalculator.calculateArea(targetPos, player, mainHand, world);
		ItemStack offHand = player.getOffhandItem();

		for (BlockPos pos : positions) {
			if (getFirstApplicableAction(world, pos, offHand) != null) return true;
		}
		return false;
	}

	/**
	 * Executes ONE stage in priority order (till -> plant -> bonemeal -> harvest):
	 * one click processes all blocks for the first stage that has targets, then stops.
	 */
	private static int executeCascadingActions(
			Player player,
			Level world,
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

	/** False for non-farming blocks so vanilla interactions still work. */
	public static boolean isHoeRelevantBlock(Level world, BlockPos pos, BlockState state) {
		return TillAction.canTill(world, pos, state)
			|| PlantAction.canPlant(world, pos, state)
			|| HarvestAction.canHarvest(world, pos)
			|| BonemealAction.canBonemealCrop(world, pos);
	}

	/** Shared by execution and preview rendering (ServerAreaRenderer). */
	public static HoeAction getFirstApplicableAction(Level world, BlockPos pos, ItemStack offHand) {
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
