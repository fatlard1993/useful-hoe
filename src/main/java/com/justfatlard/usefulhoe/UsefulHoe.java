package com.justfatlard.usefulhoe;

import com.justfatlard.usefulhoe.action.HoeActionHandler;
import com.justfatlard.usefulhoe.config.ModConfig;
import com.justfatlard.usefulhoe.render.ServerAreaRenderer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Server-side only: works with vanilla clients, no client counterpart. */
public class UsefulHoe implements ModInitializer {

	public static final String MOD_ID = "useful-hoe";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Useful Hoe (server-side)");

		ModConfig.get();

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) ->
			HoeActionHandler.handleUseBlock(player, world, hand, hitResult)
		);

		ServerAreaRenderer.register();

		// Isolated in its own class and only reached from here: it refers to
		// Village Quests types directly, so it must not load without that mod.
		if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("village-quests-justfatlard")) {
			com.justfatlard.usefulhoe.integration.HoeQuestRegistration.register();
		}

		LOGGER.info("Useful Hoe initialized");
	}
}
