package net.neoforged.neoforge.transfer;

/**
 * 索引修改器接口 - 允许直接设置槽位中的资源和数量。
 *
 * @param <T> 资源类型
 */
@FunctionalInterface
public interface IndexModifier<T> {
	void set(int index, T resource, int amount);
}
