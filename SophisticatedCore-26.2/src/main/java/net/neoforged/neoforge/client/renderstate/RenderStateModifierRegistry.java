package net.neoforged.neoforge.client.renderstate;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

/**
 * 渲染状态修改器注册表。
 * <p>
 * 由 {@code MixinLivingEntityRenderer} 在 {@code LivingEntityRenderer#extractRenderState}
 * 末尾调用 {@link #runEntityModifiers}，把实体数据写入渲染状态（两阶段渲染的提取阶段）。
 * 修改器可能在提取线程上执行，注册表使用并发集合。
 */
public final class RenderStateModifierRegistry {
	private static final List<BiConsumer<LivingEntity, LivingEntityRenderState>> GLOBAL_MODIFIERS = new CopyOnWriteArrayList<>();
	private static final Map<EntityType<?>, List<BiConsumer<LivingEntity, LivingEntityRenderState>>> MODIFIERS_BY_TYPE = new ConcurrentHashMap<>();

	private RenderStateModifierRegistry() {
	}

	public static void registerEntityModifier(@Nullable EntityType<? extends LivingEntity> entityType,
			BiConsumer<LivingEntity, LivingEntityRenderState> modifier) {
		if (entityType == null) {
			GLOBAL_MODIFIERS.add(modifier);
		} else {
			MODIFIERS_BY_TYPE.computeIfAbsent(entityType, t -> new CopyOnWriteArrayList<>()).add(modifier);
		}
	}

	public static void runEntityModifiers(LivingEntity entity, LivingEntityRenderState state) {
		for (BiConsumer<LivingEntity, LivingEntityRenderState> modifier : GLOBAL_MODIFIERS) {
			modifier.accept(entity, state);
		}

		List<BiConsumer<LivingEntity, LivingEntityRenderState>> typed = MODIFIERS_BY_TYPE.get(entity.getType());
		if (typed != null) {
			// CopyOnWriteArrayList 迭代器在创建时捕获快照数组，运行期注册不会干扰进行中的迭代，
			// 无需防御性拷贝（每帧每实体分配 ArrayList 是纯浪费）。
			for (BiConsumer<LivingEntity, LivingEntityRenderState> modifier : typed) {
				modifier.accept(entity, state);
			}
		}
	}
}
