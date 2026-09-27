package net.p3pp3rf1y.sophisticatedcore.api;

import net.minecraft.util.context.ContextKey;

/**
 * 为实体渲染状态扩展 render data 存取能力。
 * <p>
 * 该接口通过 Mixin 注入到 {@code LivingEntityRenderState} 上，
 * 外部代码可通过 {@code ((IRenderDataExtension) state).setRenderData(...)} 调用。
 * <p>
 * 这是为了替代 NeoForge 在 {@code LivingEntityRenderState} 上扩展的
 * {@code setRenderData(ContextKey<T>, T)} / {@code getRenderData(ContextKey<T>)} 方法。
 */
public interface IRenderDataExtension {

	/**
	 * 存储一个渲染数据项。
	 *
	 * @param key   上下文键
	 * @param value 数据值
	 * @param <T>   数据类型
	 */
	<T> void setRenderData(ContextKey<T> key, T value);

	/**
	 * 读取一个渲染数据项。
	 *
	 * @param key 上下文键
	 * @param <T> 数据类型
	 * @return 数据值，如果不存在返回 null
	 */
	<T> T getRenderData(ContextKey<T> key);
}
