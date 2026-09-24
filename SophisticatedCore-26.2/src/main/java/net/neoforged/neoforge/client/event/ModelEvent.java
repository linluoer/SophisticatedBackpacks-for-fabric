package net.neoforged.neoforge.client.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.minecraft.resources.Identifier;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;

/**
 * Compatibility shim for the NeoForge {@code ModelEvent} hierarchy.
 * <p>
 * This stub provides the base class plus the {@link RegisterLoaders} and
 * {@link RegisterAdditional} sub-events used by listeners that need to
 * participate in the model loading pipeline. {@link RegisterLoaders}
 * registrations are stored in a static map so the Fabric port can access
 * them via {@link #getLoaders()} to implement custom model loading.
 * Additionally, if a loader implements {@link UnbakedModelDeserializer},
 * it is automatically registered with Fabric's model deserializer registry.
 */
public abstract class ModelEvent extends Event {

	private static final Map<Identifier, UnbakedModelLoader> LOADERS = new ConcurrentHashMap<>();

	/**
	 * Fired to allow mods to register custom unbaked model loaders. The shim
	 * stores registrations in a static map accessible via
	 * {@link #getLoaders()}, and also registers them with Fabric's
	 * {@link UnbakedModelDeserializer} if the loader implements it.
	 */
	public static class RegisterLoaders extends ModelEvent {

		/**
		 * Register an unbaked model loader under the given identifier.
		 *
		 * @param id     the identifier of the loader
		 * @param loader the unbaked model loader
		 */
		public void register(Identifier id, UnbakedModelLoader loader) {
			LOADERS.put(id, loader);
			if (loader instanceof UnbakedModelDeserializer deserializer) {
				UnbakedModelDeserializer.register(id, deserializer);
			}
		}
	}

	/**
	 * Fired to allow mods to register additional models that should be baked
	 * during the model baking phase. The shim is a stub that exposes the
	 * registration method used by listeners.
	 */
	public static class RegisterAdditional extends ModelEvent {

		/**
		 * Register an additional model to be baked.
		 *
		 * @param id the identifier of the additional model
		 */
		public void register(Identifier id) {
			// no-op shim
		}
	}

	/**
	 * Get all registered unbaked model loaders keyed by their identifier.
	 * <p>
	 * 返回不可变视图而非拷贝：该方法在每个模型的加载回调中被调用（F3+T 重载时数千次），
	 * Map.copyOf 的全量拷贝是纯开销。loader 注册仅在客户端 setup 期完成后不再变化，
	 * CHM 的弱一致迭代视图对只读消费方安全。
	 *
	 * @return an unmodifiable view of the registered loaders
	 */
	public static Map<Identifier, UnbakedModelLoader> getLoaders() {
		return java.util.Collections.unmodifiableMap(LOADERS);
	}
}
