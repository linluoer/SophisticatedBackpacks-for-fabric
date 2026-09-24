package net.neoforged.neoforge.common.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Fabric shim for NeoForge CompoundIngredient.
 * MC 26.2 native Ingredient only accepts ItemLike/HolderSet, so we delegate
 * to Ingredient.of using the items from the stacks. Component matching is lost,
 * but this is only used for recipe viewer display purposes.
 */
public final class CompoundIngredient {
	private CompoundIngredient() {
	}

	public static Ingredient of(Ingredient... ingredients) {
		// MC 26.2 Ingredient doesn't have a direct way to combine ingredients,
		// so we use Ingredient.of with the first non-empty ingredient's items.
		// This is a simplified implementation for display purposes only.
		for (Ingredient ingredient : ingredients) {
			if (!ingredient.isEmpty()) {
				return ingredient;
			}
		}
		return Ingredient.of(Items.AIR);
	}

	public static Ingredient of(ItemStack... stacks) {
		if (stacks.length == 0) {
			return Ingredient.of(Items.AIR);
		}
		// Use the first stack's item as a fallback
		return Ingredient.of(stacks[0].getItem());
	}
}
