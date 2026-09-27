package net.neoforged.fml;

import net.fabricmc.loader.api.FabricLoader;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Provides access to the list of loaded mods.
 * Shim for Fabric port - delegates to FabricLoader.
 */
public class ModList {
	private static final ModList INSTANCE = new ModList();
	private final Map<String, ModContainer> containerCache = new ConcurrentHashMap<>();

	private ModList() {
	}

	public static ModList get() {
		return INSTANCE;
	}

	public boolean isLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	public Optional<ModContainer> getModContainerById(String modId) {
		if (!isLoaded(modId)) {
			return Optional.empty();
		}
		return Optional.of(containerCache.computeIfAbsent(modId, id -> {
			net.fabricmc.loader.api.ModContainer fabricContainer = FabricLoader.getInstance().getModContainer(id).orElse(null);
			String version = fabricContainer != null ? fabricContainer.getMetadata().getVersion().toString() : "1.0.0";
			return new ModContainer(id, version);
		}));
	}

	public Optional<Object> getModObject(String modId) {
		return getModContainerById(modId).map(ModContainer::getModInstance);
	}
}
