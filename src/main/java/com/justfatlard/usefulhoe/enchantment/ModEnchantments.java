package com.justfatlard.usefulhoe.enchantment;

import com.justfatlard.usefulhoe.UsefulHoe;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class ModEnchantments {

	public static final ResourceKey<Enchantment> REACH = ResourceKey.create(
		Registries.ENCHANTMENT,
		Identifier.fromNamespaceAndPath(UsefulHoe.MOD_ID, "reach")
	);

	private ModEnchantments() {}

	public static int getReachLevel(Level world, ItemStack stack) {
		if (world == null || stack.isEmpty()) return 0;

		try {
			var registry = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
			var entryOptional = registry.get(REACH);

			if (entryOptional.isEmpty()) return 0;

			Holder<Enchantment> entry = entryOptional.get();
			ItemEnchantments enchantments = stack.getEnchantments();

			return enchantments.getLevel(entry);
		} catch (Exception e) {
			UsefulHoe.LOGGER.warn("Failed to read Reach enchantment level", e);
			return 0;
		}
	}
}
