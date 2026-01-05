package com.justfatlard.usefulhoe.render;

import com.justfatlard.usefulhoe.UsefulHoe;
import com.justfatlard.usefulhoe.action.HoeAction;
import com.justfatlard.usefulhoe.action.BonemealAction;
import com.justfatlard.usefulhoe.action.HarvestAction;
import com.justfatlard.usefulhoe.action.PlantAction;
import com.justfatlard.usefulhoe.action.TillAction;
import com.justfatlard.usefulhoe.hoe.HoeAreaCalculator;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;

/**
 * Server-side area preview rendering using particles.
 * Sends particle packets to players holding hoes.
 */
public final class ServerAreaRenderer {

	private static final int TICK_INTERVAL = 4;
	private static int tickCounter = 0;

	// Particle colors as ARGB integers
	private static final int COLOR_TILL = 0xFF8B4513;     // Brown
	private static final int COLOR_PLANT = 0xFF33CC33;    // Green
	private static final int COLOR_BONEMEAL = 0xFFFFFFFF; // White
	private static final int COLOR_HARVEST = 0xFFFFD700;  // Gold

	private ServerAreaRenderer() {}

	/**
	 * Registers server tick event for particle rendering.
	 */
	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			tickCounter++;
			if (tickCounter < TICK_INTERVAL) return;
			tickCounter = 0;

			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				renderForPlayer(server, player);
			}
		});

		UsefulHoe.LOGGER.info("Server-side area preview registered");
	}

	/**
	 * Renders particle preview for a specific player.
	 */
	private static void renderForPlayer(MinecraftServer server, ServerPlayerEntity player) {
		// Check if player is holding a hoe
		ItemStack mainHand = player.getStackInHand(Hand.MAIN_HAND);
		if (!(mainHand.getItem() instanceof HoeItem)) {
			return;
		}

		// Skip if sneaking
		if (player.isSneaking()) {
			return;
		}

		// Get player's world - ServerPlayerEntity extends ServerWorld context
		ServerWorld world = (ServerWorld) player.getEntityWorld();

		// Raycast to find what block player is looking at
		BlockHitResult hitResult = raycastFromPlayer(player, 5.0);
		if (hitResult.getType() != HitResult.Type.BLOCK) {
			return;
		}

		BlockPos targetPos = hitResult.getBlockPos();
		BlockState targetState = world.getBlockState(targetPos);

		// Only show preview for hoe-relevant blocks
		if (!isHoeRelevantBlock(world, targetPos, targetState)) {
			return;
		}

		// Calculate affected area
		List<BlockPos> positions = HoeAreaCalculator.calculateArea(
			targetPos, player, mainHand, world
		);

		// Check if any action is possible
		ItemStack offHand = player.getOffHandStack();
		boolean hasAnyAction = false;
		for (BlockPos pos : positions) {
			if (getFirstApplicableAction(world, pos, offHand) != null) {
				hasAnyAction = true;
				break;
			}
		}

		if (!hasAnyAction) {
			return;
		}

		// Spawn particles for each position
		for (BlockPos pos : positions) {
			HoeAction action = getFirstApplicableAction(world, pos, offHand);
			if (action != null) {
				int color = getColorForAction(action);
				spawnCornerParticles(world, player, pos, color);
			}
		}
	}

	/**
	 * Performs a raycast from the player's eyes in their look direction.
	 */
	private static BlockHitResult raycastFromPlayer(ServerPlayerEntity player, double maxDistance) {
		Vec3d eyePos = player.getEyePos();
		Vec3d lookVec = player.getRotationVec(1.0f);
		Vec3d endPos = eyePos.add(lookVec.multiply(maxDistance));

		ServerWorld world = (ServerWorld) player.getEntityWorld();
		return world.raycast(new RaycastContext(
			eyePos,
			endPos,
			RaycastContext.ShapeType.OUTLINE,
			RaycastContext.FluidHandling.NONE,
			player
		));
	}

	/**
	 * Checks if a block is hoe-relevant.
	 */
	private static boolean isHoeRelevantBlock(World world, BlockPos pos, BlockState state) {
		if (TillAction.canTill(world, pos, state)) return true;
		if (PlantAction.canPlant(world, pos, state)) return true;
		if (HarvestAction.canHarvest(world, pos)) return true;
		if (BonemealAction.canBonemealCrop(world, pos)) return true;
		return false;
	}

	/**
	 * Gets the first applicable action for a position.
	 */
	private static HoeAction getFirstApplicableAction(World world, BlockPos pos, ItemStack offHand) {
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

	/**
	 * Gets particle color for an action.
	 */
	private static int getColorForAction(HoeAction action) {
		return switch (action) {
			case TILL -> COLOR_TILL;
			case PLANT -> COLOR_PLANT;
			case BONEMEAL -> COLOR_BONEMEAL;
			case HARVEST -> COLOR_HARVEST;
		};
	}

	/**
	 * Spawns particles at block corners, visible only to the specific player.
	 */
	private static void spawnCornerParticles(ServerWorld world, ServerPlayerEntity player, BlockPos pos, int color) {
		DustParticleEffect particle = new DustParticleEffect(color, 0.5f);

		double y = pos.getY() + 1.01;
		double x = pos.getX();
		double z = pos.getZ();
		double inset = 0.1;

		// Spawn particles only for this player
		// Signature: player, particle, force, important, x, y, z, count, deltaX, deltaY, deltaZ, speed
		world.spawnParticles(player, particle, true, false,
			x + inset, y, z + inset, 1, 0, 0, 0, 0);
		world.spawnParticles(player, particle, true, false,
			x + 1 - inset, y, z + inset, 1, 0, 0, 0, 0);
		world.spawnParticles(player, particle, true, false,
			x + inset, y, z + 1 - inset, 1, 0, 0, 0, 0);
		world.spawnParticles(player, particle, true, false,
			x + 1 - inset, y, z + 1 - inset, 1, 0, 0, 0, 0);
	}
}
