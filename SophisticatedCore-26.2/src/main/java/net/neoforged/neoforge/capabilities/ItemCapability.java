package net.neoforged.neoforge.capabilities;

/**
 * 物品能力键 - 用于查询物品栈的能力。
 *
 * @param <T> 能力类型
 * @param <C> 上下文类型
 */
public final class ItemCapability<T, C> {
	private final String name;

	private ItemCapability(String name) {
		this.name = name;
	}

	public static <T, C> ItemCapability<T, C> create(String name) {
		return new ItemCapability<>(name);
	}

	@Override
	public String toString() {
		return "ItemCapability[" + name + "]";
	}
}
