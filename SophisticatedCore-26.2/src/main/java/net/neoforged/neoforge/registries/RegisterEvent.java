package net.neoforged.neoforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * 注册事件 - 兼容性 shim 用于 Fabric 移植。
 * <p>
 * 在 NeoForge 中，此事件在注册表填充阶段触发。在 Fabric 移植中，
 * 该事件不被实际使用（注册由 DeferredRegister 直接完成），
 * 但保留 API 表面以使源码可编译。
 */
public class RegisterEvent extends Event {

	@Nullable
	private ResourceKey<? extends Registry<?>> registryKey;

	/**
	 * 向指定的注册表注册一个值。
	 *
	 * @param registryKey 注册表 ResourceKey
	 * @param name 资源名
	 * @param valueSupplier 值供应商
	 * @param <T> 注册表类型
	 */
	public <T> void register(ResourceKey<Registry<T>> registryKey, Identifier name, Supplier<T> valueSupplier) {
		// no-op shim: 实际注册由 DeferredRegister.performRegistration() 处理。
	}

	/**
	 * 获取此事件对应的注册表 ResourceKey。
	 * <p>
	 * 用于调用方根据注册表 key 过滤逻辑（例如 ModItems.registerContainers 中
	 * 只在 MENU 注册表事件触发时才注册容器类型）。在 shim 中默认返回 null，
	 * 可通过 {@link #forRegistry(ResourceKey)} 创建带 key 的事件实例。
	 *
	 * @return 注册表 ResourceKey，可能为 null
	 */
	@Nullable
	public ResourceKey<? extends Registry<?>> getRegistryKey() {
		return registryKey;
	}

	/**
	 * 创建一个带有指定 registryKey 的事件实例。便于测试和显式触发。
	 */
	public static RegisterEvent forRegistry(ResourceKey<? extends Registry<?>> key) {
		RegisterEvent event = new RegisterEvent();
		event.registryKey = key;
		return event;
	}

	/**
	 * 获取与给定 ResourceKey 对应的注册表。
	 *
	 * @param registryKey 注册表 ResourceKey
	 * @param <T> 注册表类型
	 * @return 注册表实例（如果可解析），否则 null
	 */
	@SuppressWarnings("unchecked")
	public <T> Registry<T> getRegistry(ResourceKey<Registry<T>> registryKey) {
		if (registryKey == null) {
			return null;
		}
		try {
			for (var field : BuiltInRegistries.class.getDeclaredFields()) {
				if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) && Registry.class.isAssignableFrom(field.getType())) {
					field.setAccessible(true);
					Object value = field.get(null);
					if (value instanceof Registry<?> reg && reg.key().equals(registryKey)) {
						return (Registry<T>) reg;
					}
				}
			}
		} catch (Throwable ignored) {
			// best-effort
		}
		return null;
	}
}
