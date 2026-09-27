package net.neoforged.neoforge.capabilities;

/**
 * 实体能力键 - 用于查询实体的能力。
 *
 * @param <T> 能力类型
 * @param <C> 上下文类型
 */
public final class EntityCapability<T, C> {
	private final String name;

	private EntityCapability(String name) {
		this.name = name;
	}

	public static <T, C> EntityCapability<T, C> create(String name) {
		return new EntityCapability<>(name);
	}

	@Override
	public String toString() {
		return "EntityCapability[" + name + "]";
	}
}
