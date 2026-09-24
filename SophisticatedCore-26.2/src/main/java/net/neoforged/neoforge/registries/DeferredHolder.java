package net.neoforged.neoforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 延迟持有者 - 在注册表项注册后懒加载检索值的持有者。
 * <p>
 * 兼容性 shim：为 Fabric 移植提供 NeoForge DeferredHolder API 表面。
 * 实现 {@link Supplier}，因此可作为方法参数（例如 DataComponentHolder.getOrDefault）中的
 * {@code Supplier<? extends DataComponentType<? extends T>>} 使用。
 *
 * @param <T> 注册表根类型
 * @param <R> 实际值类型（R extends T）
 */
public class DeferredHolder<T, R extends T> implements Supplier<R> {
	protected final Identifier name;
	protected final ResourceKey<T> key;
	protected final Supplier<R> factory;
	protected volatile R value;
	protected volatile boolean resolved;

	protected DeferredHolder(Identifier name, Supplier<R> factory) {
		this.name = name;
		this.factory = factory;
		this.key = null;
	}

	protected DeferredHolder(ResourceKey<T> key, Supplier<R> factory) {
		this.name = key != null ? key.identifier() : null;
		this.key = key;
		this.factory = factory;
	}

	/**
	 * 创建一个懒加载的 DeferredHolder，按需通过工厂创建值。
	 */
	public static <T, R extends T> DeferredHolder<T, R> create(Registry<T> registry, Identifier name, Supplier<R> factory) {
		return new DeferredHolder<>(name, factory);
	}

	/**
	 * 创建一个绑定到 ResourceKey 的 DeferredHolder。
	 */
	public static <T, R extends T> DeferredHolder<T, R> create(ResourceKey<T> key, Supplier<R> factory) {
		return new DeferredHolder<>(key, factory);
	}

	@Override
	public R get() {
		if (!resolved) {
			synchronized (this) {
				if (!resolved) {
					R created = factory.get();
					value = created;
					resolved = true;
					return created;
				}
			}
		}
		return value;
	}

	/**
	 * 强制刷新缓存的值，下次调用 {@link #get()} 时会重新调用工厂。
	 * 在实际注册后由 DeferredRegister 调用以绑定真实值。
	 */
	public void bind(R value) {
		synchronized (this) {
			this.value = value;
			this.resolved = true;
		}
	}

	/**
	 * 获取注册名（ResourceLocation 形式）。
	 */
	public Identifier getId() {
		return name;
	}

	/**
	 * 获取 ResourceKey（可能为 null，因为并非所有 DeferredHolder 都用 ResourceKey 构造）。
	 */
	public ResourceKey<T> getKey() {
		return key;
	}

	/**
	 * 检查值是否已可用。
	 */
	public boolean isPresent() {
		if (!resolved) {
			synchronized (this) {
				return resolved;
			}
		}
		return value != null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DeferredHolder<?, ?> other)) {
			return false;
		}
		return Objects.equals(name, other.name);
	}

	@Override
	public String toString() {
		return "DeferredHolder[" + name + "]";
	}
}
