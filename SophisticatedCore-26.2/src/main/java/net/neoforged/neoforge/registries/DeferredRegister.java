package net.neoforged.neoforge.registries;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.p3pp3rf1y.sophisticatedcore.eventbus.IEventBus;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 延迟注册器 - 兼容性 shim 用于 Fabric 移植。
 * <p>
 * 提供 NeoForge DeferredRegister 的 API 表面。当底层的 vanilla 注册表可解析时，
 * register(IEventBus) 也会尝试将条目注册到 BuiltInRegistries 中。
 * 对于自定义注册表（如 FluidType），由专用 Fabric shim（FluidTypeLookup）处理。
 * 与 IEventBus 的绑定是 no-op，因为 Fabric 使用 entrypoints。
 *
 * @param <T> 注册表的根类型
 */
public class DeferredRegister<T> {

	protected final Registry<T> registry;
	protected final ResourceKey<Registry<T>> registryKey;
	protected final Identifier registryId;
	protected final String modId;
	protected final List<DeferredHolder<T, ?>> entries = new ArrayList<>();
	protected final Map<String, DeferredHolder<T, ?>> byName = new ConcurrentHashMap<>();
	protected volatile boolean registered;

	@SuppressWarnings("unchecked")
	protected DeferredRegister(Registry<T> registry, String modId) {
		this.registry = registry;
		this.registryKey = registry != null ? (ResourceKey<Registry<T>>) registry.key() : null;
		this.registryId = registryKey != null ? registryKey.identifier() : null;
		this.modId = modId;
	}

	protected DeferredRegister(ResourceKey<Registry<T>> registryKey, String modId) {
		this.registry = resolveRegistry(registryKey);
		this.registryKey = registryKey;
		this.registryId = registryKey != null ? registryKey.identifier() : null;
		this.modId = modId;
	}

	protected DeferredRegister(Identifier registryId, String modId) {
		this.registry = null;
		this.registryKey = null;
		this.registryId = registryId;
		this.modId = modId;
	}

	/**
	 * 尝试从 BuiltInRegistries 中按 ResourceKey 解析实际注册表。如果无法解析（例如自定义注册表），返回 null。
	 */
	@SuppressWarnings("unchecked")
	private static <T> Registry<T> resolveRegistry(ResourceKey<Registry<T>> key) {
		if (key == null) {
			return null;
		}
		// Walk known BuiltInRegistries fields to find a matching registry by key.
		// This avoids relying on the REGISTRY meta-registry whose presence varies between MC versions.
		try {
			for (var field : BuiltInRegistries.class.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers()) && Registry.class.isAssignableFrom(field.getType())) {
					field.setAccessible(true);
					Object value = field.get(null);
					if (value instanceof Registry<?> reg && reg.key().equals(key)) {
						return (Registry<T>) reg;
					}
				}
			}
		} catch (Throwable ignored) {
			// best-effort: fall through and return null
		}
		return null;
	}

	/**
	 * 通过 ResourceKey 创建 DeferredRegister。
	 */
	public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> key, String modId) {
		@SuppressWarnings("unchecked")
		ResourceKey<Registry<T>> castKey = (ResourceKey<Registry<T>>) key;
		return new DeferredRegister<>(castKey, modId);
	}

	/**
	 * 通过 Registry 实例创建 DeferredRegister。
	 */
	public static <T> DeferredRegister<T> create(Registry<T> registry, String modId) {
		return new DeferredRegister<>(registry, modId);
	}

	/**
	 * 通过 Identifier 创建 DeferredRegister（用于 NeoForge 自定义注册表）。
	 */
	public static <T> DeferredRegister<T> create(Identifier registryId, String modId) {
		return new DeferredRegister<>(registryId, modId);
	}

	/**
	 * 创建专门的 Items DeferredRegister，提供 registerItem 辅助方法。
	 */
	public static DeferredRegister.Items createItems(String modId) {
		return new Items(modId);
	}

	/**
	 * 创建专门的 Blocks DeferredRegister，提供 registerBlock 辅助方法。
	 */
	public static DeferredRegister.Blocks createBlocks(String modId) {
		return new Blocks(modId);
	}

	/**
	 * 创建专门的 Entities DeferredRegister，提供 registerEntityType 辅助方法。
	 */
	public static DeferredRegister.Entities createEntities(String modId) {
		return new Entities(modId);
	}

	/**
	 * 注册一个名称到工厂的条目。
	 */
	public <R extends T> DeferredHolder<T, R> register(String name, Function<Identifier, R> factory) {
		Identifier id = Identifier.fromNamespaceAndPath(modId, name);
		DeferredHolder<T, R> holder = new DeferredHolder<>(id, () -> factory.apply(id));
		entries.add(holder);
		byName.put(name, holder);
		return holder;
	}

	/**
	 * 注册一个名称到 Supplier 的条目。
	 */
	public <R extends T> DeferredHolder<T, R> register(String name, Supplier<R> supplier) {
		Identifier id = Identifier.fromNamespaceAndPath(modId, name);
		DeferredHolder<T, R> holder = new DeferredHolder<>(id, supplier);
		entries.add(holder);
		byName.put(name, holder);
		return holder;
	}

	/**
	 * 添加别名（来自原 NeoForge API），在 shim 中作为 no-op。
	 */
	public void addAlias(Identifier from, Identifier to) {
		// no-op in shim
	}

	/**
	 * 在 shim 中是 no-op：Fabric 使用 entrypoints 注册。
	 * 仍会触发将 entries 注册到 BuiltInRegistries 中（仅对可解析的 vanilla 注册表有效）。
	 */
	public void register(IEventBus bus) {
		// bus binding is no-op for Fabric; perform best-effort vanilla registration.
		performRegistration();
	}

	/**
	 * 直接调用注册（即使没有 IEventBus）。
	 */
	public void performRegistration() {
		if (registered) {
			return;
		}
		synchronized (this) {
			if (registered) {
				return;
			}
			registered = true;
			Registry<T> resolvedRegistry = registry;
			if (resolvedRegistry == null && registryId != null) {
				// 尝试从 BuiltInRegistries 中动态查找 vanilla 注册表
				// vanilla 26.2 的 Registry.get(Identifier) 返回 Holder.Reference，需要 .value() 提取
				resolvedRegistry = (Registry<T>) BuiltInRegistries.REGISTRY.get(registryId)
						.map(Holder.Reference::value)
						.orElse(null);
			}
			if (resolvedRegistry != null) {
				for (DeferredHolder<T, ?> holder : entries) {
					registerInRegistry(holder, resolvedRegistry);
				}
			}
			// 对于 NeoForge 自定义注册表（如 FluidType），由专用 Fabric shim 处理
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	protected <R extends T> void registerInRegistry(DeferredHolder<T, R> holder, Registry<T> resolvedRegistry) {
		try {
			R value = holder.get();
			if (value != null && resolvedRegistry != null) {
				Identifier id = holder.getId();
				if (id != null && !resolvedRegistry.containsKey(id)) {
					Registry.register((Registry) resolvedRegistry, id, value);
				}
				holder.bind(value);
			}
		} catch (Throwable e) {
			System.err.println("[DeferredRegister] Failed to register " + holder.getId() + " in " + registryId + ": " + e);
			e.printStackTrace();
		}
	}

	/**
	 * 获取已注册的 DeferredHolder 列表。
	 */
	public List<DeferredHolder<T, ?>> getEntries() {
		return entries;
	}

	/**
	 * 专用于物品的 DeferredRegister，提供 registerItem 方法。
	 */
	public static class Items extends DeferredRegister<Item> {
		protected Items(String modId) {
			super(BuiltInRegistries.ITEM, modId);
		}

		/**
		 * 注册一个物品。properties 函数接收已设置 id 的 Item.Properties，返回最终物品。
		 * 在 vanilla MC 26.2 中，Item 构造器要求 Properties.id 不为 null，
		 * 因此这里自动通过 setId 设置 ResourceKey。
		 */
		public <I extends Item> DeferredHolder<Item, I> registerItem(String name, Function<Item.Properties, I> factory) {
			Identifier itemId = Identifier.fromNamespaceAndPath(modId, name);
			ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, itemId);
			return register(name, () -> factory.apply(new Item.Properties().setId(key)));
		}
	}

	/**
	 * 专用于方块的 DeferredRegister，提供 registerBlock 方法。
	 */
	public static class Blocks extends DeferredRegister<Block> {
		protected Blocks(String modId) {
			super(BuiltInRegistries.BLOCK, modId);
		}

		/**
		 * 注册一个方块。factory 函数接收已设置 id 的 BlockBehaviour.Properties，返回最终方块。
		 * 在 vanilla MC 26.2 中，BlockBehaviour 构造器调用 effectiveDrops() 要求 Properties.id 不为 null，
		 * 因此这里自动通过 setId 设置 ResourceKey。
		 */
		public <B extends Block> DeferredHolder<Block, B> registerBlock(String name, Function<BlockBehaviour.Properties, B> factory) {
			Identifier blockId = Identifier.fromNamespaceAndPath(modId, name);
			ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, blockId);
			return register(name, () -> factory.apply(BlockBehaviour.Properties.of().setId(key)));
		}
	}

	/**
	 * 专用于实体类型的 DeferredRegister，提供 registerEntityType 方法。
	 */
	public static class Entities extends DeferredRegister<EntityType<?>> {
		protected Entities(String modId) {
			super(BuiltInRegistries.ENTITY_TYPE, modId);
		}

		/**
		 * 注册一个实体类型。通过 EntityType.Builder 配置，并最终 build 出 EntityType。
		 *
		 * @param name            实体类型名称
		 * @param factory         实体工厂
		 * @param category        生物类别
		 * @param builderCustomizer 对 EntityType.Builder 的自定义回调（如设置大小、跟踪范围等）
		 * @param <E>              实体类型
		 * @return 注册的 DeferredHolder
		 */
		public <E extends Entity> DeferredHolder<EntityType<?>, EntityType<E>> registerEntityType(
				String name, EntityType.EntityFactory<E> factory, MobCategory category,
				Consumer<EntityType.Builder<E>> builderCustomizer) {
			return register(name, () -> {
				EntityType.Builder<E> builder = EntityType.Builder.of(factory, category);
				builderCustomizer.accept(builder);
				ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE,
						Identifier.fromNamespaceAndPath(modId, name));
				return builder.build(key);
			});
		}
	}
}
