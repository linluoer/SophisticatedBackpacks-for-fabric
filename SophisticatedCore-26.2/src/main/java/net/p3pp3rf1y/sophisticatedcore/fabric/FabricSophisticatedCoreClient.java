package net.p3pp3rf1y.sophisticatedcore.fabric;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import net.p3pp3rf1y.sophisticatedcore.compat.ScNeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.io.Reader;
import java.util.List;
import java.util.Map;

/**
 * SC 客户端 Fabric 入口点。
 * <p>
 * 注册 {@link ModelLoadingPlugin} 来支持 NeoForge 风格的 "loader" 字段。
 * 当模型 JSON 包含 "loader" 字段时，使用对应的 {@link UnbakedModelLoader}
 * 解析模型，替代 vanilla 的 {@link CuboidModel} 解析。
 * <p>
 * 同时触发 {@link RegisterKeyMappingsEvent} 和 {@link FMLClientSetupEvent}
 * 以激活 SC 的快捷键注册和客户端初始化逻辑，
 * 并将 Fabric 的 {@link ClientTickEvents.END_CLIENT_TICK} 桥接到
 * {@link NeoForge#EVENT_BUS} 上的 {@link ClientTickEvent.Post} 事件。
 */
public class FabricSophisticatedCoreClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ModelLoadingPlugin.register(new SophisticatedModelLoadingPlugin());

		// 触发 SC 的快捷键注册（SORT_KEYBIND、TRANSFER_TO_STORAGE_KEYBIND、TRANSFER_TO_INVENTORY_KEYBIND）
		// 以及流体模型和客户端扩展注册（XP 流体贴图等）
		ModContainer container = ModContainer.getContainer("sophisticatedcore");
		if (container != null) {
			container.getEventBus().ifPresent(bus -> {
				bus.post(new RegisterKeyMappingsEvent());
				bus.post(new RegisterFluidModelsEvent());
				bus.post(new RegisterClientExtensionsEvent());
				// 触发粒子 provider 注册（SC 的 ModParticles::registerFactories 监听此事件，
			// 通过 shim 直接调用 Fabric ParticleFactoryRegistry 完成 JUKEBOX_NOTE 粒子注册）
			bus.post(new RegisterParticleProvidersEvent());
			bus.post(new FMLClientSetupEvent());
			});
		}

		// 将 Fabric 的 START/END_CLIENT_TICK 桥接到 NeoForge EVENT_BUS 上的 ClientTickEvent.Pre/Post，
		// 使 KeybindHandler.handleKeyInputEvent 和 ClientEventHandler.onTickEnd 等监听器被调用。
		ClientTickEvents.START_CLIENT_TICK.register(client ->
				ScNeoForge.EVENT_BUS.post(new ClientTickEvent.Pre()));
		ClientTickEvents.END_CLIENT_TICK.register(client ->
				ScNeoForge.EVENT_BUS.post(new ClientTickEvent.Post()));

		registerClientLevelTickBridge();
		registerClientConnectionBridges();
	}

	// ---- Client level tick bridges ----
	// LevelTickEvent.Pre  <- ClientTickEvents.START_LEVEL_TICK
	// LevelTickEvent.Post <- ClientTickEvents.END_LEVEL_TICK
	// EntityTickEvent.Post <- iterate entities at end of level tick
	private static void registerClientLevelTickBridge() {
		ClientTickEvents.START_LEVEL_TICK.register(level ->
				ScNeoForge.EVENT_BUS.post(new LevelTickEvent.Pre(level)));

		ClientTickEvents.END_LEVEL_TICK.register(level -> {
			ScNeoForge.EVENT_BUS.post(new LevelTickEvent.Post(level));
			// EntityTickEvent.Post 已通过 MixinLivingEntity 在 LivingEntity.tick() 末尾触发，
			// 不再在此处遍历 level.players()。Player 继承自 LivingEntity，Mixin 会自动覆盖。
		});
	}

	// ---- Client connection bridges ----
	// LevelEvent.Load                <- ClientPlayConnectionEvents.JOIN (first level ready)
	// LevelEvent.Load                <- ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE (dimension change)
	// LevelEvent.Unload              <- ClientPlayConnectionEvents.DISCONNECT
	// ClientPlayerNetworkEvent.LoggingIn  <- ClientPlayConnectionEvents.JOIN
	// ClientPlayerNetworkEvent.LoggingOut <- ClientPlayConnectionEvents.DISCONNECT
	private static void registerClientConnectionBridges() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			Level level = client.level;
			if (level != null) {
				ScNeoForge.EVENT_BUS.post(new LevelEvent.Load(level));
			}
			ScNeoForge.EVENT_BUS.post(new ClientPlayerNetworkEvent.LoggingIn());
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			Level level = client.level;
			if (level != null) {
				ScNeoForge.EVENT_BUS.post(new LevelEvent.Unload(level));
			}
			ScNeoForge.EVENT_BUS.post(new ClientPlayerNetworkEvent.LoggingOut());
		});

		// Dimension change: fire LevelEvent.Load for the new ClientLevel.
		// BackpackStorage::onClientWorldLoad / BackpackTemplateStorage::onClientWorldLoad /
		// ClientBackpackContentsTooltip::onWorldLoad all clear client-side caches on level load.
		ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((client, newLevel) ->
				ScNeoForge.EVENT_BUS.post(new LevelEvent.Load(newLevel)));
	}

	/**
	 * 模型加载插件，拦截所有模型加载，检查 JSON 是否包含 "loader" 字段。
	 * 如果包含且对应的 loader 已注册，则使用该 loader 解析模型。
	 * <p>
	 * 负缓存：确认不含 loader 的模型 ID 记入 Set（同 JVM 生命周期内模型内容不变，
	 * F3+T 重载读取的是相同资源）。原实现对每个模型（包括全部 vanilla 模型）都读盘 +
	 * 完整 JSON 解析，重载时对数千个模型重复探测是显著的突发开销。
	 */
	private static class SophisticatedModelLoadingPlugin implements ModelLoadingPlugin {
		private static final java.util.Set<Identifier> MODELS_WITHOUT_LOADER = java.util.concurrent.ConcurrentHashMap.newKeySet();

		@Override
		public void initialize(Context context) {
			context.modifyModelOnLoad().register((original, onLoadContext) -> {
				Map<Identifier, UnbakedModelLoader> loaders = ModelEvent.getLoaders();
				if (loaders.isEmpty()) {
					return original;
				}

				Identifier modelId = onLoadContext.id();
				if (MODELS_WITHOUT_LOADER.contains(modelId)) {
					return original;
				}
				Identifier resourceLocation = Identifier.fromNamespaceAndPath(
						modelId.getNamespace(), "models/" + modelId.getPath() + ".json");

				try {
					Resource resource = Minecraft.getInstance().getResourceManager()
							.getResource(resourceLocation).orElse(null);
					if (resource == null) {
						return original;
					}

					try (Reader reader = resource.openAsReader()) {
						JsonElement element = GsonHelper.parse(reader);
						if (!element.isJsonObject()) {
							MODELS_WITHOUT_LOADER.add(modelId);
							return original;
						}
						JsonObject json = element.getAsJsonObject();
						if (!json.has("loader")) {
							MODELS_WITHOUT_LOADER.add(modelId);
							return original;
						}

						Identifier loaderId = Identifier.parse(GsonHelper.getAsString(json, "loader"));
						UnbakedModelLoader loader = loaders.get(loaderId);
						if (loader == null) {
							return original;
						}

						UnbakedModel custom = loader.read(json, CuboidModel.GSON);
						return custom != null ? custom : original;
					}
				} catch (Exception e) {
					return original;
				}
			});
		}
	}
}
