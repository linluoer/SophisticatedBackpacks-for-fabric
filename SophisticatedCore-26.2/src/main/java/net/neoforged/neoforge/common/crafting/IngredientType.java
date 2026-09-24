package net.neoforged.neoforge.common.crafting;

import com.mojang.serialization.MapCodec;

/**
 * IngredientType shim - represents a type of custom ingredient for serialization.
 */
public record IngredientType<T extends ICustomIngredient>(MapCodec<T> codec) {
}
