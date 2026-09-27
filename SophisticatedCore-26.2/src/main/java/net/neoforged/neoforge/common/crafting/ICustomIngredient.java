package net.neoforged.neoforge.common.crafting;

import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.stream.Stream;

/**
 * ICustomIngredient shim - interface for custom recipe ingredients.
 */
public interface ICustomIngredient {
	boolean test(net.minecraft.world.item.ItemStack stack);

	Stream<net.minecraft.core.Holder<net.minecraft.world.item.Item>> items();

	default boolean isSimple() {
		return false;
	}

	IngredientType<?> getType();

	SlotDisplay display();
}
