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
	 * Called during mod init to ensure class loading.
	 */
	public static void initialize() {
		// Enchantment is data-driven, no registration needed
	}

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

			// getOptional returns Optional<Reference<T>> which IS a RegistryEntry
			RegistryEntry<Enchantment> entry = entryOptional.get();
			ItemEnchantmentsComponent enchantments = stack.getEnchantments();

			return enchantments.getLevel(entry);
		} catch (Exception e) {
			return 0;
		}
	}
}
