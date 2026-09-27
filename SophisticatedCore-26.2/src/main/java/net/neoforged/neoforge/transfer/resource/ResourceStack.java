package net.neoforged.neoforge.transfer.resource;

/**
 * 资源堆 - 持有资源和数量的记录。
 *
 * @param <T> 资源类型
 */
public record ResourceStack<T>(T resource, int amount) {
	public static <T> ResourceStack<T> empty() {
		return new ResourceStack<>(null, 0);
	}

	public boolean isEmpty() {
		return resource == null || amount <= 0;
	}
}
