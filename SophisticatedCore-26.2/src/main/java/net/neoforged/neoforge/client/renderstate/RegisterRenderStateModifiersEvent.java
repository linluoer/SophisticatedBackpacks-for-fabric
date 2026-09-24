package net.neoforged.neoforge.client.renderstate;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * Compatibility shim for {@code RegisterRenderStateModifiersEvent}.
 * <p>
 * 注册的实体修改器会在每帧的提取阶段（{@code LivingEntityRenderer#extractRenderState} 末尾，
 * 由 {@code MixinLivingEntityRenderer} 注入调用）执行，用于把实体上的自定义数据
 * 写入 {@link LivingEntityRenderState}，供渲染层在绘制阶段读取。
 */
public class RegisterRenderStateModifiersEvent extends Event {

	/**
	 * Register a render-state modifier for the given entity type.
	 *
	 * @param entityType entity type the modifier applies to; {@code null} registers a
	 *                   global modifier that runs for every living entity
	 * @param modifier   the modifier invoked at the end of extraction
	 */
	public void registerEntityModifier(@Nullable EntityType<? extends LivingEntity> entityType,
			BiConsumer<LivingEntity, LivingEntityRenderState> modifier) {
		RenderStateModifierRegistry.registerEntityModifier(entityType, modifier);
	}
}
