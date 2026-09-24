package net.neoforged.neoforge.common.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

/**
 * 全局战利品修改器接口 shim。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。在 NeoForge 中，全局战利品修改器会在战利品表生成后被调用，
 * 可以根据条件修改生成的战利品。本 shim 仅提供 API 表面，
 * 实际的逻辑由 {@link LootModifier} 等基类实现。
 */
public interface IGlobalLootModifier {

	/**
	 * 用于将 {@link LootItemCondition} 数组与编解码器交互的辅助编解码器。
	 * <p>
	 * 将 {@link LootItemCondition#DIRECT_CODEC} 列表与数组互相转换。
	 */
	Codec<LootItemCondition[]> LOOT_CONDITIONS_CODEC = LootItemCondition.DIRECT_CODEC.listOf()
			.xmap(list -> list.toArray(new LootItemCondition[0]), List::of);

	/**
	 * 返回此修改器的 {@link MapCodec}，用于序列化/反序列化。
	 *
	 * @return 修改器的 MapCodec
	 */
	MapCodec<? extends IGlobalLootModifier> codec();

	/**
	 * 将修改器应用于生成的战利品列表。
	 * <p>
	 * 默认实现直接返回原列表（no-op）。
	 *
	 * @param generatedLoot 战利品表生成的物品列表
	 * @param context       战利品上下文
	 * @return 修改后的战利品列表
	 */
	default ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		return generatedLoot;
	}
}
