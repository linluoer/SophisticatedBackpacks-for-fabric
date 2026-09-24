package net.p3pp3rf1y.sophisticatedbackpacks.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.p3pp3rf1y.sophisticatedbackpacks.SophisticatedBackpacks;
import net.p3pp3rf1y.sophisticatedbackpacks.client.render.BackpackBlockModel;
import net.p3pp3rf1y.sophisticatedbackpacks.client.render.BackpackLayerRenderer;

import java.util.Locale;
import java.util.Map;

/**
 * 客户端 Fabric 入口点类。
 * <p>
 * 在客户端环境中 post 各类注册事件，触发 SB 的客户端监听器：
 * <ul>
 *   <li>{@link RegisterClientTooltipComponentFactoriesEvent} - 自定义 tooltip 组件工厂</li>
 *   <li>{@link RegisterItemModelsEvent} - 自定义物品模型类型</li>
 *   <li>{@link RegisterBlockStateModels} - 自定义方块状态模型类型</li>
 *   <li>{@link RegisterColorHandlersEvent.ItemTintSources} - 自定义物品 tint source 类型</li>
 *   <li>{@link RegisterColorHandlersEvent.BlockTintSources} - 方块 tint source 注册（延迟应用到 BlockColors）</li>
 *   <li>{@link net.neoforged.neoforge.client.event.ModelEvent.RegisterLoaders} - 自定义模型加载器</li>
 *   <li>{@link RegisterMenuScreensEvent} - 菜单屏幕注册</li>
 *   <li>{@link EntityRenderersEvent.RegisterRenderers} - 实体/方块实体渲染器注册</li>
 *   <li>{@link AddClientReloadListenersEvent} - 客户端资源重载监听器注册</li>
 * </ul>
 * <p>
 * main entrypoint（{@link FabricSophisticatedBackpacks}）先于 client entrypoint 执行，
 * 因此 {@link ModContainer} 和 {@link IEventBus} 已就绪。
 */
public class FabricSophisticatedBackpacksClient implements ClientModInitializer {
	private static boolean blockTintsApplied = false;

	@Override
	public void onInitializeClient() {
		// 预发现背包 part 模型，确保贴图被正确注册到纹理图集
		ModelLoadingPlugin.register(context -> {
			for (BackpackBlockModel.ModelPart part : BackpackBlockModel.ModelPart.values()) {
				Identifier partId = SophisticatedBackpacks.getIdentifier("block/backpack_" + part.name().toLowerCase(Locale.ENGLISH));
				ExtraModelKey<BlockStateModel> key = ExtraModelKey.create();
				context.addModel(key, SimpleUnbakedExtraModel.blockStateModel(partId));
			}
		});

		// 为所有 LivingEntity 渲染器（含玩家 AvatarRenderer）添加背包渲染层。
		// 渲染层本身在无渲染数据时不绘制，怪物穿背包也会经 RENDER_STATE_MODIFIER 提取数据。
		LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) ->
				registerBackpackLayer(entityRenderer, registrationHelper));

		ModContainer container = ModContainer.getContainer("sophisticatedbackpacks");
		if (container != null) {
			container.getEventBus().ifPresent(bus -> {
				bus.post(new RegisterClientTooltipComponentFactoriesEvent());
			bus.post(new RegisterItemModelsEvent());
			bus.post(new RegisterBlockStateModels());
			bus.post(new RegisterColorHandlersEvent.ItemTintSources());
			bus.post(new RegisterColorHandlersEvent.BlockTintSources());
			bus.post(new net.neoforged.neoforge.client.event.ModelEvent.RegisterLoaders());
			bus.post(new RegisterMenuScreensEvent());
			bus.post(new EntityRenderersEvent.RegisterRenderers());
			bus.post(new RegisterRenderStateModifiersEvent());
			bus.post(new AddClientReloadListenersEvent());
			// 触发 SB 的快捷键注册（BACKPACK_OPEN、INVENTORY_INTERACTION、TOOL_SWAP、UPGRADE_SLOT_TOGGLE）
		bus.post(new RegisterKeyMappingsEvent());
			// 触发 FMLClientSetupEvent，使 SophisticatedBackpacks::clientSetup 调用 KeybindHandler.register()
			bus.post(new FMLClientSetupEvent());
			});
			// 将 AddClientReloadListenersEvent 收集的重载监听器桥接到 Fabric API
			for (Map.Entry<Identifier, PreparableReloadListener> entry : AddClientReloadListenersEvent.drainPendingListeners()) {
				Identifier id = entry.getKey();
				PreparableReloadListener delegate = entry.getValue();
				IdentifiableResourceReloadListener wrapped = new IdentifiableResourceReloadListener() {
					@Override
					public Identifier getFabricId() { return id; }

					@Override
					public java.util.concurrent.CompletableFuture<Void> reload(
							PreparableReloadListener.SharedState currentReload, java.util.concurrent.Executor taskExecutor,
							PreparableReloadListener.PreparationBarrier preparationBarrier, java.util.concurrent.Executor reloadExecutor) {
						return delegate.reload(currentReload, taskExecutor, preparationBarrier, reloadExecutor);
					}
				};
				ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(wrapped);
			}
		}
		// BlockColors 在 Minecraft 构造器中创建，onInitializeClient 早于构造器，
		// 因此延迟到首个客户端 tick 应用缓冲的 block tint source 注册。
		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			if (!blockTintsApplied) {
				blockTintsApplied = true;
				RegisterColorHandlersEvent.applyBlockTintSources(Minecraft.getInstance().getBlockColors());
			}
		});
	}

	/**
	 * 向实体渲染器添加背包渲染层。泛型通配符需要借助原始类型完成桥接。
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void registerBackpackLayer(LivingEntityRenderer<?, ?, ?> entityRenderer,
			LivingEntityRenderLayerRegistrationCallback.RegistrationHelper registrationHelper) {
		registrationHelper.register(new BackpackLayerRenderer(entityRenderer));
	}
}
