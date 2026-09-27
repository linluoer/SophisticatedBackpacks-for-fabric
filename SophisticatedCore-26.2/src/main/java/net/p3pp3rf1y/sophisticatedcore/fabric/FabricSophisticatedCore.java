package net.p3pp3rf1y.sophisticatedcore.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.p3pp3rf1y.sophisticatedcore.eventbus.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.p3pp3rf1y.sophisticatedcore.compat.ScNeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.network.SyncRecipesPayload;

import java.util.List;

/**
 * Fabric 入口点类。
 * <p>
 * 原始 {@link SophisticatedCore} 使用 NeoForge 的 {@code @Mod} 注解和构造器注入模式，
 * 在 Fabric 中不会被加载器调用。此类实现 {@link ModInitializer}，在 {@code onInitialize()}
 * 中创建必要的 stub 对象（{@link IEventBus}、{@link Dist}、{@link ModContainer}）并实例化
 * 原始类，从而复用其全部初始化逻辑。
 * <p>
 * 此外，此类负责将 Fabric 生命周期/交互事件桥接到 NeoForge {@link NeoForge#EVENT_BUS}，
 * 使 SC 与 SB 中通过 {@code eventBus.addListener(...)} 注册的监听器能被正确触发。
 */
public class FabricSophisticatedCore implements ModInitializer {
	@Override
	public void onInitialize() {
		String version = FabricLoader.getInstance().getModContainer(SophisticatedCore.MOD_ID)
				.map(c -> c.getMetadata().getVersion().toString())
				.orElse("1.0.0");
		ModContainer container = new ModContainer(SophisticatedCore.MOD_ID, version);
		IEventBus modBus = container.getEventBus().orElseGet(ModContainer::createEventBus);
		Dist dist = FMLEnvironment.getDist();
		SophisticatedCore instance = new SophisticatedCore(modBus, dist, container);
		container.setModInstance(instance);
		// 触发网络 payload 注册（构造器中已注册监听器，需要 post 事件才会执行）
		modBus.post(new RegisterPayloadHandlersEvent());

		// 捕获服务器引用和线程组，使 BackpackStorage 等 SavedData 能在服务器线程上正确工作。
		// ServerLifecycleHooks.setServer 会缓存 MinecraftServer 并调用 SidedThreadGroups.initServer()。
		ServerLifecycleEvents.SERVER_STARTING.register(server -> ServerLifecycleHooks.setServer(server));
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> ServerLifecycleHooks.setServer(null));

		registerServerTickBridges();
		registerServerLifecycleBridges();
		registerPlayerConnectionBridges();
		registerPlayerProgressionBridges();
		registerCommandBridge();
		registerEntityUnloadBridge();
		registerInteractionBridges();

		// Sync full recipe holders to the client on player join. Vanilla MC 26.2 only syncs recipe
		// displays (ClientRecipeContainer), but JEI/REI recipe viewers and RecipeHelper's
		// compacting/uncompacting logic need the full RecipeHolder collection on the client. In
		// single-player the integrated server's RecipeManager is used directly; in multiplayer this
		// sync populates RecipeHelper.RECIPE_MAP so ClientRecipeHelper.transformAllRecipesOfType and
		// RecipeHelper.getRecipesOfType return the custom recipes (BackpackUpgradeRecipe,
		// SmithingBackpackUpgradeRecipe, UpgradeNextTierRecipe, ...).
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayer player = handler.getPlayer();
			List<net.minecraft.world.item.crafting.RecipeHolder<?>> recipes = List.copyOf(server.getRecipeManager().getRecipes());
			// 分批发送配方，避免单个网络包超出大小限制导致客户端 DecoderException。
			// 1653 个配方分 9 批（每批 200 个）发送。
			int batchSize = SyncRecipesPayload.BATCH_SIZE;
			int totalBatches = (recipes.size() + batchSize - 1) / batchSize;
			for (int i = 0; i < totalBatches; i++) {
				int from = i * batchSize;
				int to = Math.min(from + batchSize, recipes.size());
				List<net.minecraft.world.item.crafting.RecipeHolder<?>> batch = recipes.subList(from, to);
				PacketDistributor.sendToPlayer(player, new SyncRecipesPayload(i, totalBatches, batch));
			}
		});
	}

	// ---- Server tick bridges ----
	// ServerTickEvent.Post <- ServerTickEvents.END_SERVER_TICK
	// LevelTickEvent.Pre  <- ServerTickEvents.START_LEVEL_TICK (server)
	// LevelTickEvent.Post <- ServerTickEvents.END_LEVEL_TICK   (server)
	private static void registerServerTickBridges() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			ScNeoForge.EVENT_BUS.post(new ServerTickEvent.Post(server, true));
			// 在 server tick 末尾检测玩家维度变化（Fabric API 此版本无 AFTER_CHANGE_DIMENSION 事件）
			detectDimensionChanges(server);
		});

		ServerTickEvents.START_LEVEL_TICK.register(level ->
				ScNeoForge.EVENT_BUS.post(new LevelTickEvent.Pre(level)));

		ServerTickEvents.END_LEVEL_TICK.register(level -> {
			ScNeoForge.EVENT_BUS.post(new LevelTickEvent.Post(level));
			// EntityTickEvent.Post 已通过 MixinLivingEntity 在 LivingEntity.tick() 末尾触发，
			// 不再在此处遍历 level.getAllEntities()。旧方式每 tick 遍历所有实体（包括掉落物、
			// 箭矢等非 LivingEntity），性能开销巨大且曾导致死循环。Mixin 方式只在 LivingEntity
			// 实际被 tick 时触发事件，与 NeoForge 原始行为一致。
		});
	}

	// ---- 维度变化检测 ----
	// Fabric API 5.0.5 没有 ServerPlayerEvents.AFTER_CHANGE_DIMENSION，
	// 因此在每 tick 末尾对比每个玩家的当前维度与缓存维度，若变化则 post PlayerChangedDimensionEvent。
	private static final java.util.Map<java.util.UUID, net.minecraft.resources.ResourceKey<Level>> PLAYER_DIMENSIONS = new java.util.concurrent.ConcurrentHashMap<>();

	private static void detectDimensionChanges(MinecraftServer server) {
		// PlayerList.players 只在 server 线程的 tick 循环内被修改（加入/离开均排队到 tick 处理），
		// 本方法同样运行在 server 线程，直接迭代即可；每 tick 的 List.copyOf 是纯分配开销。
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			net.minecraft.resources.ResourceKey<Level> current = player.level().dimension();
			net.minecraft.resources.ResourceKey<Level> previous = PLAYER_DIMENSIONS.put(player.getUUID(), current);
			if (previous != null && !previous.equals(current)) {
				ScNeoForge.EVENT_BUS.post(new PlayerEvent.PlayerChangedDimensionEvent(player));
			}
		}
	}

	// ---- Server lifecycle bridges ----
	// SERVER_STARTED  -> ServerStartedEvent + LevelEvent.Load (each ServerLevel) + AddServerReloadListenersEvent (drain & register)
	// SERVER_STOPPING -> LevelEvent.Unload (each ServerLevel) + ServerStoppedEvent
	private static void registerServerLifecycleBridges() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			ScNeoForge.EVENT_BUS.post(new ServerStartedEvent(server));
			// 对每个已加载的 ServerLevel 触发 LevelEvent.Load，
			// 触发 SophisticatedCore::overworldLoaded / BackpackStorage::onWorldLoad 等
			for (ServerLevel level : server.getAllLevels()) {
				ScNeoForge.EVENT_BUS.post(new LevelEvent.Load(level));
			}
			// 触发 AddServerReloadListenersEvent 收集 server-side reload listener，
			// 然后通过 Fabric ResourceManagerHelper 注册它们。
			AddServerReloadListenersEvent reloadEvent = new AddServerReloadListenersEvent(server);
			ScNeoForge.EVENT_BUS.post(reloadEvent);
			ResourceManagerHelper helper = ResourceManagerHelper.get(PackType.SERVER_DATA);
			for (AddServerReloadListenersEvent.ListenerEntry entry : reloadEvent.getEntries()) {
				registerIdentifiableReloadListener(helper, entry.name(), entry.listener());
			}
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			// 先触发 LevelEvent.Unload（对应 MagnetUpgradeWrapper / ServerStorageSoundHandler / CoreFakePlayer）
			for (ServerLevel level : server.getAllLevels()) {
				ScNeoForge.EVENT_BUS.post(new LevelEvent.Unload(level));
			}
			ScNeoForge.EVENT_BUS.post(new ServerStoppedEvent(server));
		});
	}

	/**
	 * 已注册的 reload listener id 集合。
	 * <p>
	 * 单机中每次打开世界 integrated server 都会触发一次 SERVER_STARTED，
	 * 若不防重，第二次进世界会向全局 ResourceManagerHelper 重复注册同名 listener，
	 * 被 Fabric 的 ResourceLoaderImpl.checkUniqueResourceReloader 以
	 * "Tried to register resource listener ... twice" 拒绝并导致 server tick loop 崩溃。
	 */
	private static final java.util.Set<Identifier> REGISTERED_RELOAD_LISTENER_IDS = java.util.concurrent.ConcurrentHashMap.newKeySet();

	/**
	 * 包装一个 PreparableReloadListener 为 IdentifiableResourceReloadListener 并注册到 Fabric。
	 * Fabric API 要求注册的 listener 实现 IdentifiableResourceReloadListener 以提供 id。
	 */
	private static void registerIdentifiableReloadListener(ResourceManagerHelper helper, Identifier id, PreparableReloadListener delegate) {
		// 幂等保护：同一 id 只注册一次，重复触发（退出存档后再进世界）直接跳过
		if (!REGISTERED_RELOAD_LISTENER_IDS.add(id)) {
			return;
		}
		net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener wrapped = new net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return id;
			}

			@Override
			public java.util.concurrent.CompletableFuture<Void> reload(
					PreparableReloadListener.SharedState currentReload, java.util.concurrent.Executor taskExecutor,
					PreparableReloadListener.PreparationBarrier preparationBarrier, java.util.concurrent.Executor reloadExecutor) {
				return delegate.reload(currentReload, taskExecutor, preparationBarrier, reloadExecutor);
			}
		};
		helper.registerReloadListener(wrapped);
	}

	// ---- Player connection bridges ----
	// PlayerLoggedInEvent  <- ServerPlayConnectionEvents.JOIN
	// 维度缓存清理          <- ServerPlayConnectionEvents.DISCONNECT
	private static void registerPlayerConnectionBridges() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			Player player = handler.getPlayer();
			PLAYER_DIMENSIONS.put(player.getUUID(), player.level().dimension());
			ScNeoForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedInEvent(player));
			// Fire OnDatapackSyncEvent to clear server-side recipe caches.
			// sendRecipes() is a no-op (recipes are synced via SyncRecipesPayload).
			ScNeoForge.EVENT_BUS.post(new OnDatapackSyncEvent());
		});

		// 修复内存泄漏：PLAYER_DIMENSIONS 原先只在 JOIN 时写入、从不清理，
		// 长时间运行的服务器上条目随断线玩家 UUID 无限累积。
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
				PLAYER_DIMENSIONS.remove(handler.getPlayer().getUUID()));
	}

	// ---- Player progression bridges ----
	// PlayerRespawnEvent <- ServerPlayerEvents.AFTER_RESPAWN
	// PlayerChangedDimensionEvent <- 在 detectDimensionChanges 中处理（见上）
	private static void registerPlayerProgressionBridges() {
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			// 重生后更新维度缓存，避免误触发维度变化事件
			PLAYER_DIMENSIONS.put(newPlayer.getUUID(), newPlayer.level().dimension());
			ScNeoForge.EVENT_BUS.post(new PlayerEvent.PlayerRespawnEvent(newPlayer, false));
		});
	}

	// ---- Command bridge ----
	// RegisterCommandsEvent <- CommandRegistrationCallback
	private static void registerCommandBridge() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				ScNeoForge.EVENT_BUS.post(new RegisterCommandsEvent(dispatcher, registryAccess)));
	}

	// ---- Entity unload bridge ----
	// EntityLeaveLevelEvent <- ServerEntityEvents.ENTITY_UNLOAD
	// ServerLevel 本身就是 Level，无需 instanceof 检查
	private static void registerEntityUnloadBridge() {
		ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) ->
				ScNeoForge.EVENT_BUS.post(new EntityLeaveLevelEvent(entity, level)));
	}

	// ---- Interaction bridges ----
	// PlayerInteractEvent.LeftClickBlock  <- AttackBlockCallback
	// AttackEntityEvent                   <- AttackEntityCallback
	// PlayerInteractEvent.RightClickBlock <- UseBlockCallback
	// BreakBlockEvent                     <- PlayerBlockBreakEvents.BEFORE
	private static void registerInteractionBridges() {
		AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
			PlayerInteractEvent.LeftClickBlock event = new PlayerInteractEvent.LeftClickBlock(player, hand, pos, direction);
			ScNeoForge.EVENT_BUS.post(event);
			if (event.isCanceled()) {
				return event.getCancellationResult();
			}
			return InteractionResult.PASS;
		});

		AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
			AttackEntityEvent event = new AttackEntityEvent(player, entity);
			ScNeoForge.EVENT_BUS.post(event);
			if (event.isCanceled()) {
				return InteractionResult.FAIL;
			}
			return InteractionResult.PASS;
		});

		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			BlockPos pos = hitResult.getBlockPos();
			Direction face = hitResult.getDirection();
			PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(player, hand, pos, face);
			ScNeoForge.EVENT_BUS.post(event);
			if (event.isCanceled()) {
				return event.getCancellationResult();
			}
			return InteractionResult.PASS;
		});

		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
			if (!(world instanceof Level level)) {
				return true;
			}
			BreakBlockEvent event = new BreakBlockEvent(level, pos, state, player);
			ScNeoForge.EVENT_BUS.post(event);
			if (event.isCanceled()) {
				return false;
			}
			return true;
		});
	}
}
