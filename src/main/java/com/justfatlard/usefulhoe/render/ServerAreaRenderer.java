package com.justfatlard.usefulhoe.render;

import com.justfatlard.usefulhoe.UsefulHoe;
import com.justfatlard.usefulhoe.config.ModConfig;
import com.justfatlard.usefulhoe.action.HoeAction;
import com.justfatlard.usefulhoe.action.HoeActionHandler;
import com.justfatlard.usefulhoe.hoe.HoeAreaCalculator;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Server-side area preview rendering using particles.
 * Sends particle packets to players holding hoes, one bundle per preview.
 */
public final class ServerAreaRenderer {

	private static int tickCounter = 0;

	private static final int COLOR_TILL = 0xFF8B4513;     // Brown
	private static final int COLOR_PLANT = 0xFF33CC33;    // Green
	private static final int COLOR_BONEMEAL = 0xFFFFFFFF; // White
	private static final int COLOR_HARVEST = 0xFFFFD700;  // Gold

	/** Under vanilla's bundle limit of 4096 packets. */
	private static final int MAX_BUNDLE = 4000;

	private ServerAreaRenderer() {}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			ModConfig config = ModConfig.get();
			if (!config.particlePreviewEnabled) return;

			tickCounter++;
			if (tickCounter < config.particleTickInterval) return;
			tickCounter = 0;

			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				renderForPlayer(server, player);
			}
		});

		UsefulHoe.LOGGER.info("Server-side area preview registered");
	}

	private static void renderForPlayer(MinecraftServer server, ServerPlayer player) {
		ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
		if (!mainHand.is(ItemTags.HOES)) {
			return;
		}

		if (player.isShiftKeyDown()) {
			return;
		}

		ServerLevel world = (ServerLevel) player.level();

		BlockHitResult hitResult = raycastFromPlayer(player, 5.0);
		if (hitResult.getType() != HitResult.Type.BLOCK) {
			return;
		}

		BlockPos targetPos = hitResult.getBlockPos();
		BlockState targetState = world.getBlockState(targetPos);

		if (!HoeActionHandler.isHoeRelevantBlock(world, targetPos, targetState)) {
			return;
		}

		List<BlockPos> positions = HoeAreaCalculator.calculateArea(
			targetPos, player, mainHand, world
		);

		ItemStack offHand = player.getOffhandItem();
		List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
		for (BlockPos pos : positions) {
			HoeAction action = HoeActionHandler.getFirstApplicableAction(world, pos, offHand);
			if (action != null) {
				addCornerParticles(packets, pos, getColorForAction(action));
			}
		}

		for (int from = 0; from < packets.size(); from += MAX_BUNDLE) {
			List<Packet<? super ClientGamePacketListener>> part =
				packets.subList(from, Math.min(packets.size(), from + MAX_BUNDLE));
			player.connection.send(new ClientboundBundlePacket(new ArrayList<>(part)));
		}
	}

	private static BlockHitResult raycastFromPlayer(ServerPlayer player, double maxDistance) {
		Vec3 eyePos = player.getEyePosition();
		Vec3 lookVec = player.getViewVector(1.0f);
		Vec3 endPos = eyePos.add(lookVec.scale(maxDistance));

		ServerLevel world = (ServerLevel) player.level();
		return world.clip(new ClipContext(
			eyePos,
			endPos,
			ClipContext.Block.OUTLINE,
			ClipContext.Fluid.NONE,
			player
		));
	}

	private static int getColorForAction(HoeAction action) {
		return switch (action) {
			case TILL -> COLOR_TILL;
			case PLANT -> COLOR_PLANT;
			case BONEMEAL -> COLOR_BONEMEAL;
			case HARVEST -> COLOR_HARVEST;
		};
	}

	/**
	 * Particles at block corners, for the one player whose preview it is.
	 */
	private static void addCornerParticles(List<Packet<? super ClientGamePacketListener>> packets, BlockPos pos, int color) {
		DustParticleOptions particle = new DustParticleOptions(color, 0.5f);

		double y = pos.getY() + 1.01;
		double x = pos.getX();
		double z = pos.getZ();
		double inset = 0.1;

		packets.add(corner(particle, x + inset, y, z + inset));
		packets.add(corner(particle, x + 1 - inset, y, z + inset));
		packets.add(corner(particle, x + inset, y, z + 1 - inset));
		packets.add(corner(particle, x + 1 - inset, y, z + 1 - inset));
	}

	private static ClientboundLevelParticlesPacket corner(DustParticleOptions particle, double x, double y, double z) {
		// Signature: particle, overrideLimiter, alwaysShow, x, y, z, xDist, yDist, zDist, speed, count
		return new ClientboundLevelParticlesPacket(particle, true, false, x, y, z, 0, 0, 0, 0, 1);
	}
}
