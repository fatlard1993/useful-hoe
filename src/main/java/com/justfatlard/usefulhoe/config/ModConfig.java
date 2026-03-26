package com.justfatlard.usefulhoe.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.justfatlard.usefulhoe.UsefulHoe;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JSON config loaded once from config/useful-hoe.json at server start.
 * Writes defaults on first run. Changes require server restart.
 */
public final class ModConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = "useful-hoe.json";
	private static final int MAX_AREA_DIMENSION = 32;

	private static ModConfig instance;

	// --- Area sizes per reach level [width, depth] ---
	public int[] reach0 = {1, 1};
	public int[] reach1 = {1, 3};
	public int[] reach2 = {4, 4};
	public int[] reach3 = {4, 9};
	public int[] reach4 = {9, 9};
	public int[] reach5 = {9, 18};

	// --- Durability ---
	public int durabilityBaseCost = 1;
	public int durabilityPerBlock = 1;

	// --- Particle preview ---
	public boolean particlePreviewEnabled = true;
	public int particleTickInterval = 4;

	// --- Action toggles ---
	public boolean tillEnabled = true;
	public boolean plantEnabled = true;
	public boolean bonemealEnabled = true;
	public boolean harvestEnabled = true;

	private ModConfig() {}

	public static ModConfig get() {
		if (instance == null) {
			instance = load();
		}
		return instance;
	}

	public int[] getReachArea(int level) {
		return switch (level) {
			case 1 -> reach1;
			case 2 -> reach2;
			case 3 -> reach3;
			case 4 -> reach4;
			case 5 -> reach5;
			default -> reach0;
		};
	}

	/**
	 * Clamps all values to safe ranges to prevent exploits or crashes.
	 */
	private void validate() {
		reach0 = clampArea(reach0);
		reach1 = clampArea(reach1);
		reach2 = clampArea(reach2);
		reach3 = clampArea(reach3);
		reach4 = clampArea(reach4);
		reach5 = clampArea(reach5);

		durabilityBaseCost = Math.max(0, durabilityBaseCost);
		durabilityPerBlock = Math.max(0, durabilityPerBlock);
		particleTickInterval = Math.max(1, particleTickInterval);
	}

	private static int[] clampArea(int[] area) {
		if (area == null || area.length < 2) return new int[]{1, 1};
		return new int[]{
			Math.max(1, Math.min(area[0], MAX_AREA_DIMENSION)),
			Math.max(1, Math.min(area[1], MAX_AREA_DIMENSION))
		};
	}

	private static ModConfig load() {
		Path configDir = FabricLoader.getInstance().getConfigDir();
		Path configFile = configDir.resolve(FILE_NAME);

		if (Files.exists(configFile)) {
			try {
				String json = Files.readString(configFile);
				ModConfig config = GSON.fromJson(json, ModConfig.class);
				if (config != null) {
					config.validate();
					save(config, configFile);
					UsefulHoe.LOGGER.info("Loaded config from {}", configFile);
					return config;
				}
			} catch (Exception e) {
				UsefulHoe.LOGGER.warn("Failed to load config, using defaults: {}", e.getMessage());
			}
		}

		ModConfig config = new ModConfig();
		save(config, configFile);
		UsefulHoe.LOGGER.info("Created default config at {}", configFile);
		return config;
	}

	private static void save(ModConfig config, Path configFile) {
		try {
			Files.writeString(configFile, GSON.toJson(config));
		} catch (IOException e) {
			UsefulHoe.LOGGER.warn("Failed to save config: {}", e.getMessage());
		}
	}
}
