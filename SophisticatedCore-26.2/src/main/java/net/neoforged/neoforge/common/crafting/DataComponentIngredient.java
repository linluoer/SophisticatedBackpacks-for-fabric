package net.neoforged.neoforge.common.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Fabric shim for NeoForge DataComponentIngredient.
 * MC 26.2 native Ingredient only accepts ItemLike/HolderSet, so we delegate
 * to Ingredient.of(item) using the stack's item. Component matching is lost,
 * but this is only used for recipe viewer display purposes.
 */
public final class DataComponentIngredient {
	private DataComponentIngredient() {
	}

	public static Ingredient of(boolean strict, ItemStack... stacks) {
		if (stacks.length == 0) {
			throw new IllegalArgumentException("DataComponentIngredient requires at least one stack");
		}
		return Ingredient.of(stacks[0].getItem());
	}
}
