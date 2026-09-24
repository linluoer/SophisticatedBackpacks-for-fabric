package net.neoforged.neoforge.common.loot;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.function.Predicate;

/**
 * 全局战利品修改器抽象基类 shim。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。负责处理 {@link LootItemCondition} 条件匹配，
 * 子类只需实现 {@link #doApply(ObjectArrayList, LootContext)} 即可。
 * <p>
 * 与标准 NeoForge LootModifier 不同，本 shim 在构造函数与编解码器中包含 {@code priority} 字段，
 * 以便与 Sophisticated 系列模组的 4 参构造函数模式兼容。
 */
public abstract class LootModifier implements IGlobalLootModifier {

	protected final LootItemCondition[] conditions;
	private final Predicate<LootContext> combinedConditions;
	protected final int priority;

	/**
	 * 构造一个 LootModifier。
	 *
	 * @param conditionsIn 需要在战利品修改前匹配的 ILootCondition 数组
	 * @param priority     修改器的优先级（数值越小越先执行）
	 */
	protected LootModifier(LootItemCondition[] conditionsIn, int priority) {
		this.conditions = conditionsIn;
		this.combinedConditions = andConditions(conditionsIn);
		this.priority = priority;
	}

	private static Predicate<LootContext> andConditions(LootItemCondition[] conditions) {
		if (conditions == null || conditions.length == 0) {
			return c -> true;
		}
		Predicate<LootContext> result = conditions[0];
		for (int i = 1; i < conditions.length; i++) {
			result = result.and(conditions[i]);
		}
		return result;
	}

	/**
	 * 简化子类编解码器的创建。返回的 Products.P2 同时包含
	 * {@code conditions}（{@link LootItemCondition} 数组）和 {@code priority}（int）两个字段。
	 * <p>
	 * 子类典型用法：
	 * <pre>{@code
	 * public static final MapCodec<MyModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst)
	 *         .and(inst.group(CODEC_FOR_FIELD.fieldOf("my_field").forGetter(m -> m.myField)))
	 *         .apply(inst, MyModifier::new));
	 * }</pre>
	 *
	 * @param instance 编解码器实例
	 * @param <T>       LootModifier 子类型
	 * @return 包含 conditions 和 priority 字段的 Products.P2
	 */
	protected static <T extends LootModifier> Products.P2<RecordCodecBuilder.Mu<T>, LootItemCondition[], Integer> codecStart(
			RecordCodecBuilder.Instance<T> instance) {
		return instance.group(
				IGlobalLootModifier.LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(m -> m.conditions),
				Codec.INT.fieldOf("priority").orElse(1000).forGetter(m -> m.priority)
		);
	}

	@Override
	public final ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		return combinedConditions.test(context) ? doApply(generatedLoot, context) : generatedLoot;
	}

	/**
	 * 在所有战利品条件均已匹配后，应用修改器到生成的战利品。
	 *
	 * @param generatedLoot 战利品表生成的物品列表
	 * @param context        战利品上下文
	 * @return 修改后的战利品列表
	 */
	protected abstract ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context);

	public LootItemCondition[] getConditions() {
		return conditions;
	}

	public int getPriority() {
		return priority;
	}
}
