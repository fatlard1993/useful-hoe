package com.justfatlard.usefulhoe.integration;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Lets the hoe's area work on dirt-slab's half-height ground.
 *
 * <p>The area already reaches them - dirt-slab puts its slabs in {@code #minecraft:dirt}, which
 * is what the area scan looks for - but the till itself did nothing, because the rule for what
 * turns into what is a map of vanilla blocks and a dirt slab is not one of them. So the slabs sat
 * inside the highlighted rectangle and were the only thing in it that never changed, which reads
 * as the reach not covering them.
 *
 * <p>Matched by registry id rather than by class, so there is no dependency on dirt-slab to
 * compile or to run: with the mod absent nothing resolves, the map is empty, and every lookup
 * politely says no.
 *
 * <p>The pairs are dirt-slab's own, copied from what a single right-click there already does.
 * Guessing at them would be worse than not having them: a hoe that turns a slab into something
 * the mod that owns it would not have is a bug that looks like a feature.
 */
public final class DirtSlabTilling {
	private static final String DIRT_SLAB = "dirt-slab-justfatlard";

	/** Resolved on first use, which is gameplay - long after every mod has registered. */
	private static Map<Block, Block> pairs;

	private DirtSlabTilling() {}

	/**
	 * What this becomes when tilled, or null if it is not dirt-slab's to till.
	 *
	 * @param state the block being tilled
	 */
	public static BlockState tilled(BlockState state) {
		Block result = pairs().get(state.getBlock());
		return result == null ? null : carryOver(state, result.defaultBlockState());
	}

	private static Map<Block, Block> pairs() {
		if (pairs == null) {
			Map<Block, Block> resolved = new IdentityHashMap<>();
			pair(resolved, "coarse_dirt_slab", "dirt_slab");
			pair(resolved, "dirt_slab", "farmland_slab");
			pair(resolved, "grass_slab", "farmland_slab");
			pair(resolved, "grass_path_slab", "farmland_slab");
			pairs = resolved;
		}
		return pairs;
	}

	private static void pair(Map<Block, Block> into, String from, String to) {
		Block before = lookUp(from);
		Block after = lookUp(to);
		if (before != null && after != null) into.put(before, after);
	}

	private static Block lookUp(String path) {
		return BuiltInRegistries.BLOCK
			.getOptional(Identifier.fromNamespaceAndPath(DIRT_SLAB, path))
			.orElse(null);
	}

	/** Whatever the two have in common: which half of the block it fills, and whether it is flooded. */
	private static BlockState carryOver(BlockState from, BlockState to) {
		for (Property<?> property : from.getProperties()) {
			if (to.hasProperty(property)) to = carryOne(from, to, property);
		}
		return to;
	}

	private static <T extends Comparable<T>> BlockState carryOne(BlockState from, BlockState to, Property<T> property) {
		return to.setValue(property, from.getValue(property));
	}
}
