package net.p3pp3rf1y.sophisticatedcore.mixin;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.context.ContextKey;
import net.p3pp3rf1y.sophisticatedcore.api.IRenderDataExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;

/**
 * 给 {@link LivingEntityRenderState} 添加 NeoForge 扩展的 render data API。
 * <p>
 * 通过实现 {@link IRenderDataExtension} 接口，让 LivingEntityRenderState 实例可被 cast 到
 * {@code IRenderDataExtension}，从而调用 {@code setRenderData} / {@code getRenderData}。
 * <p>
 * 这是 NeoForge 在 LivingEntityRenderState 上扩展的接口方法的等效实现。
 */
@Mixin(LivingEntityRenderState.class)
public class MixinLivingEntityRenderState implements IRenderDataExtension {

	@Unique
	private final Map<ContextKey<?>, Object> sophisticatedcore$renderData = new HashMap<>();

	@Override
	public <T> void setRenderData(ContextKey<T> key, T value) {
		sophisticatedcore$renderData.put(key, value);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getRenderData(ContextKey<T> key) {
		return (T) sophisticatedcore$renderData.get(key);
	}
}
