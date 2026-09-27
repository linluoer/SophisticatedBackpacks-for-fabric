package net.neoforged.fml;

import net.fabricmc.loader.api.FabricLoader;
import net.p3pp3rf1y.sophisticatedcore.eventbus.IEventBus;
import net.neoforged.fml.config.ActiveModConfig;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a loaded mod container. Simplified shim for Fabric port.
 * Holds mod metadata, config registrations, and an event bus.
 */
public class ModContainer {
	private static final Logger LOGGER = LoggerFactory.getLogger("SophisticatedCore/ModContainer");
	private static final Map<String, ModContainer> containers = new ConcurrentHashMap<>();

	private final String modId;
	private final ModInfo modInfo;
	private final IEventBus eventBus;
	private Object modInstance;
	private final Map<ModConfig.Type, ActiveModConfig> configs = new ConcurrentHashMap<>();

	public ModContainer(String modId) {
		this(modId, "1.0.0");
	}

	public ModContainer(String modId, String version) {
		this.modId = modId;
		this.modInfo = new ModInfo(modId, version);
		this.eventBus = createEventBus();
		containers.put(modId, this);
	}

	/**
	 * 创建 IEventBus 实例。
	 * <p>
	 * 事件总线 shim 已迁至本 mod 自己的命名空间（{@code net.p3pp3rf1y.sophisticatedcore.eventbus}），
	 * 不再与 CreativeCore 等 mod 引入的 NeoForge bus 7.2.0 存在同包同名竞争，
	 * 因此可以直接调用而无需反射探测。
	 */
	public static IEventBus createEventBus() {
		return IEventBus.create();
	}

	public String getModId() {
		return modId;
	}

	public ModInfo getModInfo() {
		return modInfo;
	}

	public Object getModInstance() {
		return modInstance;
	}

	public void setModInstance(Object instance) {
		this.modInstance = instance;
	}

	public Optional<IEventBus> getEventBus() {
		return Optional.of(eventBus);
	}

	public void registerConfig(ModConfig.Type type, ModConfigSpec spec) {
		registerConfig(type, spec, null);
	}

	public void registerConfig(ModConfig.Type type, ModConfigSpec spec, String fileName) {
		configs.put(type, new ActiveModConfig(type, spec));
		if (!tryLoadJsonConfig(spec, resolveFileName(type, fileName)) && !spec.isLoaded()) {
			ensureConfigLoaded(type, spec, fileName);
		}
	}

	private String resolveFileName(ModConfig.Type type, String fileName) {
		if (fileName == null || fileName.isBlank()) {
			return modId + "-" + type.name().toLowerCase(Locale.ROOT) + ".json";
		}
		return fileName;
	}

	private Path resolveConfigPath(String fileName) {
		Path configDirectory = FabricLoader.getInstance().getConfigDir().toAbsolutePath().normalize();
		Path path = configDirectory.resolve(fileName).normalize();
		if (!path.startsWith(configDirectory) || path.getFileName() == null) {
			throw new IllegalArgumentException("Config file must stay inside the config directory: " + fileName);
		}
		return path;
	}

	private boolean tryLoadJsonConfig(ModConfigSpec spec, String fileName) {
		try {
			Method load = spec.getClass().getMethod("load", Path.class);
			load.invoke(spec, resolveConfigPath(fileName));
			return true;
		} catch (NoSuchMethodException e) {
			return false;
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Failed to load JSON config for " + modId + " from " + fileName, e);
		}
	}

	/**
	 * 当 spec 未加载时，通过反射加载配置。
	 * 独立运行时（无 ForgeConfigAPIPort）：spec 是我们的简化 ModConfigSpec，走方案3 setConfig。
	 * 整合包中（有 ForgeConfigAPIPort）：FCAP 是 library mod，其 ModConfigSpec 会被加载并实现 IConfigSpec，
	 * 走方案1 通过 ConfigRegistry.register() 注册（官方方式，会创建文件并加载）。
	 * 注意：catch Throwable 而非 Exception，因为 NoSuchMethodError 是 Error 不是 Exception。
	 */
	private void ensureConfigLoaded(ModConfig.Type type, ModConfigSpec spec, String fileName) {
		LOGGER.debug("[ConfigCompat] ensureConfigLoaded called for mod={}, type={}, fileName={}", modId, type, fileName);

		// 检测 ForgeConfigAPIPort 是否存在（通过 IConfigSpec 接口是否存在来判断）
		Class<?> iConfigSpecClass = null;
		try {
			iConfigSpecClass = Class.forName("net.neoforged.fml.config.IConfigSpec");
		} catch (ClassNotFoundException ignored) {
			// ForgeConfigAPIPort 不存在
		}

		boolean fcapPresent = iConfigSpecClass != null && iConfigSpecClass.isInstance(spec);
		LOGGER.debug("[ConfigCompat] ForgeConfigAPIPort present: {}", fcapPresent);

		// 独立运行时，直接走方案3（我们的 ModConfigSpec 有 setConfig 方法）
		if (!fcapPresent) {
			tryInvokeSetConfig(spec, type);
			return;
		}

		// 方案1：通过 ForgeConfigAPIPort 的 ConfigRegistry.register() 加载（官方方式，会创建文件并加载）
		if (tryRegisterViaFCAP(type, spec, fileName, iConfigSpecClass) && spec.isLoaded()) {
			return;
		}

		if (spec.isLoaded()) return;

		// 方案2：直接调用 acceptConfig(ILoadedConfig) 加载内存配置（FCAP 的 ModConfigSpec 用 acceptConfig）
		if (tryAcceptConfigInMemory(spec, type, iConfigSpecClass)) {
			return;
		}

		// 方案3：最终兜底，尝试 setConfig（我们的 ModConfigSpec 有此方法）
		tryInvokeSetConfig(spec, type);
	}

	/**
	 * 方案1：通过 ForgeConfigAPIPort 的 ConfigRegistry.register() 注册配置。
	 */
	private boolean tryRegisterViaFCAP(ModConfig.Type type, ModConfigSpec spec, String fileName, Class<?> iConfigSpecClass) {
		try {
			Class<?> configRegistryClass = Class.forName("fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry");
			Object instance = configRegistryClass.getField("INSTANCE").get(null);

			// 优先查找参数类型为 IConfigSpec 的 register 方法（NeoForge 版本）
			// 避免 net.minecraftforge.fml.config.IConfigSpec<?> 的 Forge 版本
			Method registerMethod = null;
			int paramCount = fileName != null ? 4 : 3;
			for (Method m : configRegistryClass.getMethods()) {
				if (!m.getName().equals("register") || m.getParameterCount() != paramCount) continue;
				Class<?> specParam = m.getParameterTypes()[2];
				if (!iConfigSpecClass.isAssignableFrom(specParam)) continue;
				registerMethod = m;
				break;
			}

			if (registerMethod == null) {
				LOGGER.warn("[ConfigCompat] No suitable register method found on ConfigRegistry");
				return false;
			}
			LOGGER.debug("[ConfigCompat] Found register method: {}", registerMethod);

			// 尝试直接调用（type 枚举可能来自不同的类加载器）
			try {
				if (fileName != null) {
					registerMethod.invoke(instance, modId, type, spec, fileName);
				} else {
					registerMethod.invoke(instance, modId, type, spec);
				}
			} catch (IllegalArgumentException e) {
				// ModConfig.Type 枚举来自不同的类，尝试转换
				LOGGER.debug("[ConfigCompat] Type mismatch, trying conversion");
				Object convertedType = convertEnumType(type, registerMethod.getParameterTypes()[1]);
				if (convertedType == null) {
					LOGGER.warn("[ConfigCompat] Type conversion failed for {}", type);
					return false;
				}
				if (fileName != null) {
					registerMethod.invoke(instance, modId, convertedType, spec, fileName);
				} else {
					registerMethod.invoke(instance, modId, convertedType, spec);
				}
			}

			if (spec.isLoaded()) {
				LOGGER.info("[ConfigCompat] Config loaded via ForgeConfigAPIPort for mod={}, type={}", modId, type);
				return true;
			}
		} catch (Throwable e) {
			LOGGER.debug("[ConfigCompat] ForgeConfigAPIPort registration failed", e);
		}
		return false;
	}

	/**
	 * 方案2：直接调用 acceptConfig(ILoadedConfig) 加载内存配置。
	 */
	private boolean tryAcceptConfigInMemory(ModConfigSpec spec, ModConfig.Type type, Class<?> iConfigSpecClass) {
		try {
			Class<?> iLoadedConfigClass = Class.forName("net.neoforged.fml.config.IConfigSpec$ILoadedConfig");
			Class<?> commentedConfigClass = Class.forName("com.electronwill.nightconfig.core.CommentedConfig");
			Object commentedConfig = commentedConfigClass.getMethod("inMemory").invoke(null);
			Object loadedConfig = java.lang.reflect.Proxy.newProxyInstance(
					iLoadedConfigClass.getClassLoader(),
					new Class<?>[] { iLoadedConfigClass },
					(proxy, method, args) -> {
						if (method.getName().equals("config")) return commentedConfig;
						if (method.getName().equals("save")) return null;
						return null;
					});
			spec.getClass().getMethod("acceptConfig", iLoadedConfigClass).invoke(spec, loadedConfig);
			LOGGER.info("[ConfigCompat] Config loaded via acceptConfig (in-memory) for mod={}, type={}", modId, type);
			return true;
		} catch (Throwable e) {
			LOGGER.debug("[ConfigCompat] acceptConfig fallback failed", e);
		}
		return false;
	}

	/**
	 * 方案3：最终兜底，调用 setConfig（我们的 ModConfigSpec 有此方法）。
	 * 用反射避免 NoSuchMethodError（ForgeConfigAPIPort 的类没有此方法）。
	 */
	private void tryInvokeSetConfig(ModConfigSpec spec, ModConfig.Type type) {
		try {
			Method setConfig = spec.getClass().getMethod("setConfig", Object.class);
			setConfig.invoke(spec, (Object) null);
			LOGGER.info("[ConfigCompat] Config loaded via setConfig (fallback) for mod={}, type={}", modId, type);
		} catch (Throwable e) {
			LOGGER.warn("[ConfigCompat] All config loading methods failed for mod={}, type={}", modId, type, e);
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private Object convertEnumType(ModConfig.Type type, Class<?> targetEnumClass) {
		if (!targetEnumClass.isEnum()) return null;
		try {
			return Enum.valueOf((Class<Enum>) targetEnumClass, type.name());
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}

	public ActiveModConfig getConfig(ModConfig.Type type) {
		return configs.get(type);
	}

	@SuppressWarnings("unchecked")
	public <T> void registerExtensionPoint(Class<T> extensionPoint, T extension) {
		// No-op: extension points are NeoForge-specific, not needed in Fabric
	}

	public static ModContainer getContainer(String modId) {
		return containers.get(modId);
	}

	public static Optional<ModContainer> getOptional(String modId) {
		return Optional.ofNullable(containers.get(modId));
	}

	static void registerContainer(ModContainer container) {
		containers.put(container.getModId(), container);
	}
}
