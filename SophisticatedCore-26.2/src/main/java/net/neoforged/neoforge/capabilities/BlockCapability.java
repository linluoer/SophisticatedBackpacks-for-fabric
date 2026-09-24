package net.neoforged.neoforge.capabilities;

/**
 * 方块能力键 - 用于查询方块实体的能力。
 * 简化实现：仅作为类型化键，不实际注册能力提供者。
 *
 * @param <T> 能力类型
 * @param <C> 上下文类型
 */
public final class BlockCapability<T, C> {
	private final String name;

	private BlockCapability(String name) {
		this.name = name;
	}

	public static <T, C> BlockCapability<T, C> create(String name) {
		return new BlockCapability<>(name);
	}

	@Override
	public String toString() {
		return "BlockCapability[" + name + "]";
	}
}
