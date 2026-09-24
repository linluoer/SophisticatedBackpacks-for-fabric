package net.neoforged.neoforge.common;

import net.minecraft.core.component.DataComponentType;

/**
 * 可变数据组件持有者接口 - 提供对数据组件的读写访问。
 * ItemStack已实现getOrDefault和set方法，可通过mixin使其实现此接口。
 */
public interface MutableDataComponentHolder {
	<T> T getOrDefault(DataComponentType<T> type, T defaultValue);

	<T> void set(DataComponentType<T> type, T value);

	<T> boolean has(DataComponentType<T> type);

	<T> void remove(DataComponentType<T> type);
}
