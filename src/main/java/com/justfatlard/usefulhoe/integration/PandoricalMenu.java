package com.justfatlard.usefulhoe.integration;

import com.justfatlard.usefulhoe.UsefulHoe;
import com.justfatlard.usefulhoe.config.ModConfig;
import net.fabricmc.loader.api.FabricLoader;

/** The config file's knobs in Pandorical's mod menu, for ops, when Pandorical is there. */
public final class PandoricalMenu {
	private PandoricalMenu() {}

	public static void register() {
		if (FabricLoader.getInstance().isModLoaded("pandorical")) Reach.register();
	}

	private static final class Reach {
		static void register() {
			var group = justfatlard.pandorical.api.PandoricalApi.settings().serverGroup(UsefulHoe.MOD_ID, "Useful Hoe");
			group.toggle("till", "Tilling", true).backedBy(p -> c().tillEnabled, (p, v) -> { c().tillEnabled = v; ModConfig.save(); });
			group.toggle("plant", "Planting", true).backedBy(p -> c().plantEnabled, (p, v) -> { c().plantEnabled = v; ModConfig.save(); });
			group.toggle("bonemeal", "Bonemealing", true).backedBy(p -> c().bonemealEnabled, (p, v) -> { c().bonemealEnabled = v; ModConfig.save(); });
			group.toggle("harvest", "Harvesting", true).backedBy(p -> c().harvestEnabled, (p, v) -> { c().harvestEnabled = v; ModConfig.save(); });
			group.toggle("preview", "Particle preview", true).backedBy(p -> c().particlePreviewEnabled, (p, v) -> { c().particlePreviewEnabled = v; ModConfig.save(); });
			group.number("durabilityBase", "Durability per use", 0, 10, 1, 1).backedBy(p -> c().durabilityBaseCost, (p, v) -> { c().durabilityBaseCost = v; ModConfig.save(); });
			group.number("durabilityPerBlock", "Durability per block", 0, 10, 1, 1).backedBy(p -> c().durabilityPerBlock, (p, v) -> { c().durabilityPerBlock = v; ModConfig.save(); });
			// Rain growth lives in Pandorical now, as a rule of the world; asked on, as it always was here.
			// A Pandorical from before the rules has no such call, and is left to run without it.
			try {
				justfatlard.pandorical.api.PandoricalApi.rules().rainGrowth(true);
			} catch (LinkageError older) {
				UsefulHoe.LOGGER.warn("[{}] Pandorical is older than 15.37.0, so rain does not grow crops", UsefulHoe.MOD_ID);
			}
		}

		private static ModConfig c() { return ModConfig.get(); }
	}
}
