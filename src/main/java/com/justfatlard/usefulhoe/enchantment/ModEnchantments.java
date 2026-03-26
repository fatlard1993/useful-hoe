package com.justfatlard.usefulhoe.enchantment;

import com.justfatlard.usefulhoe.UsefulHoe;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * Registry keys and utilities for mod enchantments.
 */
public final class ModEnchantments {

	public static final RegistryKey<Enchantment> REACH = RegistryKey.of(
		RegistryKeys.ENCHANTMENT,
		Identifier.of(UsefulHoe.MOD_ID, "reach")
	);

	private ModEnchantments() {}

	/**
	 * Gets the reach enchantment level from an item stack.
	 * @return The enchantment level, or 0 if not enchanted
	 */
	public static int getReachLevel(World world, ItemStack stack) {
		if (world == null || stack.isEmpty()) return 0;

		try {
			var registry = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
			var entryOptional = registry.getOptional(REACH);

			if (entryOptional.isEmpty()) return 0;

			RegistryEntry<Enchantment> entry = entryOptional.get();
			ItemEnchantmentsComponent enchantments = stack.getEnchantments();

			return enchantments.getLevel(entry);
		} catch (Exception e) {
			UsefulHoe.LOGGER.warn("Failed to read Reach enchantment level", e);
			return 0;
		}
	}
}
