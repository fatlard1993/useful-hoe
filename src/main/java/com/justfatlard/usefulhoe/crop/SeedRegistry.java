package com.justfatlard.usefulhoe.crop;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public final class SeedRegistry {

	private static final Map<Block, Item> CROP_TO_SEED = new HashMap<>();
	private static final Map<Item, Block> SEED_TO_CROP = new HashMap<>();

	static {
		register(Blocks.WHEAT, Items.WHEAT_SEEDS);
		register(Blocks.CARROTS, Items.CARROT);
		register(Blocks.POTATOES, Items.POTATO);
		register(Blocks.BEETROOTS, Items.BEETROOT_SEEDS);
		register(Blocks.NETHER_WART, Items.NETHER_WART);
		register(Blocks.MELON_STEM, Items.MELON_SEEDS);
		register(Blocks.PUMPKIN_STEM, Items.PUMPKIN_SEEDS);
		register(Blocks.TORCHFLOWER_CROP, Items.TORCHFLOWER_SEEDS);
		register(Blocks.PITCHER_CROP, Items.PITCHER_POD);
	}

	private static void register(Block crop, Item seed) {
		CROP_TO_SEED.put(crop, seed);
		SEED_TO_CROP.put(seed, crop);
	}

	private SeedRegistry() {}

	public static Item getSeedFor(Block crop) {
		return CROP_TO_SEED.get(crop);
	}

	public static boolean isSeed(Item item) {
		return SEED_TO_CROP.containsKey(item);
	}

	public static Block getCropFor(Item seed) {
		return SEED_TO_CROP.get(seed);
	}
}
