package net.p3pp3rf1y.sophisticatedcore.eventbus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 事件总线接口与简单实现，服务于本移植版的 NeoForge 风格事件代码。
 * <p>
 * 刻意放在本 mod 自己的命名空间下，详见 {@link Event} 的说明。
 */
public interface IEventBus {
	<T extends Event> void addListener(Consumer<T> consumer);

	<T extends Event> void addListener(Class<T> eventType, Consumer<T> consumer);

	/**
	 * 添加带有优先级的监听器。
	 */
	default <T extends Event> void addListener(EventPriority priority, Consumer<T> consumer) {
		addListener(consumer);
	}

	/**
	 * 添加带有优先级和指定事件类型的监听器。
	 */
	default <T extends Event> void addListener(EventPriority priority, Class<T> eventType, Consumer<T> consumer) {
		addListener(eventType, consumer);
	}

	/**
	 * 移除监听器。在简化 shim 中为 best-effort（按对象引用移除）。
	 */
	default <T extends Event> boolean removeListener(Consumer<T> consumer) {
		return false;
	}

	void register(Object target);

	void unregister(Object target);

	<T extends Event> T post(T event);

	/**
	 * 事件类型（含其继承链上的父类型）是否有任何监听器。
	 * <p>
	 * 供高频触发点（如实体 tick mixin）在分配事件对象前先短路：
	 * 无监听器时跳过事件构造与分发，避免每实体每 tick 的纯浪费分配。
	 */
	default boolean hasListeners(Class<? extends Event> eventType) {
		return false;
	}

	/**
	 * Simple in-memory implementation of IEventBus.
	 */
	class Impl implements IEventBus {
		private final Map<Class<?>, List<Consumer<?>>> listeners = new ConcurrentHashMap<>();

		/**
		 * 事件类 → 合并后的监听器列表缓存。
		 * <p>
		 * post() 原先每次沿继承链逐级 map 查找（如 Post -> EntityTickEvent -> Event 共 3 级），
		 * 实体 tick 等高频事件下每秒数千次重复解析。继承链结构对每个事件类固定，
		 * 监听器注册又只在启动期发生，用 ClassValue 缓存合并结果后每次 post 降为一次查找。
		 * 列表本身不可变快照（注册时重建），无需同步。
		 * <p>
		 * ClassValue 无失效 API：监听器变化时替换整个 cache 实例（volatile 保证可见性），
		 * 旧实例随 GC 回收。注册只发生在启动期，替换开销可忽略。
		 */
		private static final class DispatchCache extends ClassValue<List<Consumer<?>>> {
			private final Map<Class<?>, List<Consumer<?>>> listeners;

			private DispatchCache(Map<Class<?>, List<Consumer<?>>> listeners) {
				this.listeners = listeners;
			}

			@Override
			protected List<Consumer<?>> computeValue(Class<?> eventType) {
				List<Consumer<?>> merged = new ArrayList<>();
				Class<?> clazz = eventType;
				while (Event.class.isAssignableFrom(clazz)) {
					List<Consumer<?>> eventListeners = listeners.get(clazz);
					if (eventListeners != null) {
						merged.addAll(eventListeners);
					}
					clazz = clazz.getSuperclass();
				}
				return List.copyOf(merged);
			}
		}

		private volatile DispatchCache dispatchCache = new DispatchCache(listeners);

		@Override
		public <T extends Event> void addListener(Consumer<T> consumer) {
			Class<?> eventType = resolveEventType(consumer);
			listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(consumer);
			invalidateDispatchCache();
		}

		@Override
		public <T extends Event> void addListener(Class<T> eventType, Consumer<T> consumer) {
			listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(consumer);
			invalidateDispatchCache();
		}

		@Override
		public <T extends Event> void addListener(EventPriority priority, Consumer<T> consumer) {
			addListener(consumer);
		}

		@Override
		public <T extends Event> void addListener(EventPriority priority, Class<T> eventType, Consumer<T> consumer) {
			addListener(eventType, consumer);
		}

		@Override
		public <T extends Event> boolean removeListener(Consumer<T> consumer) {
			boolean removed = false;
			for (List<Consumer<?>> list : listeners.values()) {
				if (list.remove(consumer)) {
					removed = true;
				}
			}
			if (removed) {
				invalidateDispatchCache();
			}
			return removed;
		}

		@Override
		public void register(Object target) {
			// No-op: simplified shim does not support reflection-based registration
		}

		@Override
		public void unregister(Object target) {
			// No-op: simplified shim does not support reflection-based unregistration
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T extends Event> T post(T event) {
			// ClassValue 缓存命中后为单次查找零迭代器分配；
			// Event.class 兜底列表中的监听器以类型过滤（解析失败的监听器期望具体事件类型）。
			for (Consumer<?> listener : dispatchCache.get(event.getClass())) {
				try {
					((Consumer<Event>) listener).accept(event);
				} catch (ClassCastException ignored) {
					// 兜底列表中的监听器可能期望其他具体事件类型，跳过
				}
			}
			return event;
		}

		@Override
		public boolean hasListeners(Class<? extends Event> eventType) {
			return !dispatchCache.get(eventType).isEmpty();
		}

		private void invalidateDispatchCache() {
			// 替换 cache 实例使旧 ClassValue 缓存整体失效；注册只发生在启动期，开销可忽略
			dispatchCache = new DispatchCache(listeners);
		}

		private Class<?> resolveEventType(Consumer<?> consumer) {
			// 性能关键：用 typetools 解析 Consumer<T> 的泛型事件类型（与 NeoForge bus 7 的做法一致），
			// 让监听器注册到精确事件类型下，post() 通过 Map 直达对应列表。
			// <p>
			// 旧实现无条件返回 Event.class，导致所有监听器挤在同一列表：
			// 每次 post 都遍历全部监听器并依赖抛出/捕获 ClassCastException 过滤类型。
			// 实体 tick 等高频事件在密集实体场景（如百头猪的猪圈）下每秒产生约 10 万个异常，
			// 构造异常的 fillInStackTrace 开销是严重的性能瓶颈。
			// 解析失败时回退 Event.class 保持旧行为。
			try {
				Class<?> resolved = net.jodah.typetools.TypeResolver.resolveRawArgument(Consumer.class, consumer.getClass());
				if (resolved != net.jodah.typetools.TypeResolver.Unknown.class
						&& resolved != Object.class
						&& Event.class.isAssignableFrom(resolved)) {
					return resolved;
				}
			} catch (Throwable ignored) {
				// lambda 泛型信息不可得时走 fallback
			}
			return Event.class;
		}
	}

	static IEventBus create() {
		return new Impl();
	}
}
