package com.justfatlard.usefulhoe;

import com.justfatlard.usefulhoe.action.HoeActionHandler;
import com.justfatlard.usefulhoe.config.ModConfig;
import com.justfatlard.usefulhoe.render.ServerAreaRenderer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod initializer for Useful Hoe.
 * Server-side mod - works with vanilla clients on multiplayer.
 * Also works in singleplayer.
 */
public class UsefulHoe implements ModInitializer {

	public static final String MOD_ID = "useful-hoe";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Useful Hoe (server-side)");

		ModConfig.get();

		// Register block use callback for hoe actions
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) ->
			HoeActionHandler.handleUseBlock(player, world, hand, hitResult)
		);

		// Register server-side particle preview
		ServerAreaRenderer.register();

		LOGGER.info("Useful Hoe initialized");
	}
}
