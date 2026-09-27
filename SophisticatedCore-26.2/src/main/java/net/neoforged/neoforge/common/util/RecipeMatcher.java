package net.neoforged.neoforge.common.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

/**
 * RecipeMatcher shim - matches crafting inputs against recipe ingredients.
 */
public final class RecipeMatcher {
	private RecipeMatcher() {
	}

	public static int[] findMatches(List<ItemStack> inputs, List<Ingredient> ingredients) {
		if (inputs.size() != ingredients.size()) {
			return null;
		}

		int[] data = new int[inputs.size()];
		for (int i = 0; i < data.length; i++) {
			data[i] = -1;
		}

		return match(inputs, ingredients, data, 0) ? data : null;
	}

	private static boolean match(List<ItemStack> inputs, List<Ingredient> ingredients, int[] data, int current) {
		if (current == inputs.size()) {
			return true;
		}

		ItemStack input = inputs.get(current);
		for (int i = 0; i < ingredients.size(); i++) {
			if (alreadyUsed(data, i, current)) {
				continue;
			}
			if (ingredients.get(i).test(input)) {
				data[current] = i;
				if (match(inputs, ingredients, data, current + 1)) {
					return true;
				}
				data[current] = -1;
			}
		}

		return false;
	}

	private static boolean alreadyUsed(int[] data, int index, int current) {
		for (int i = 0; i < current; i++) {
			if (data[i] == index) {
				return true;
			}
		}
		return false;
	}
}
