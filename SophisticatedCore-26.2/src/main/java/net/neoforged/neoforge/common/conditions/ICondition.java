package net.neoforged.neoforge.common.conditions;

import com.mojang.serialization.MapCodec;

/**
 * ICondition shim - interface for recipe conditions.
 */
public interface ICondition {
	boolean test(IContext context);

	MapCodec<? extends ICondition> codec();

	interface IContext {
		IContext EMPTY = tag -> false;

		boolean tag(net.minecraft.tags.TagKey<net.minecraft.world.item.Item> tag);
	}
}
