package net.p3pp3rf1y.sophisticatedbackpacks.client;

import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.p3pp3rf1y.sophisticatedcore.eventbus.IEventBus;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.p3pp3rf1y.sophisticatedcore.compat.ScNeoForge;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.p3pp3rf1y.sophisticatedbackpacks.SophisticatedBackpacks;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackShapes;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackStorage;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackTemplateStorage;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.client.init.BackpackTintSources;
import net.p3pp3rf1y.sophisticatedbackpacks.client.init.ModBlockColors;
import net.p3pp3rf1y.sophisticatedbackpacks.client.render.*;
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedbackpacks.network.BlockPickPayload;
import net.p3pp3rf1y.sophisticatedbackpacks.network.RequestPlayerSettingsPayload;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;
import net.p3pp3rf1y.sophisticatedcore.api.IUpgradeClientTickHandler;
import net.p3pp3rf1y.sophisticatedcore.client.render.UpgradeClientRegistry;
import net.p3pp3rf1y.sophisticatedcore.renderdata.IUpgradeClientData;
import net.p3pp3rf1y.sophisticatedcore.renderdata.RenderDataHandler;
import net.p3pp3rf1y.sophisticatedcore.renderdata.UpgradeClientDataType;
import org.joml.Vector3f;

import java.util.Map;

import static net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.EVERLASTING_BACKPACK_ITEM_ENTITY;

public class ClientEventHandler {
	private ClientEventHandler() {
	}

	private static final String BACKPACK_REG_NAME = "backpack";
	public static final ModelLayerLocation BACKPACK_LAYER = new ModelLayerLocation(
			Identifier.fromNamespaceAndPath(SophisticatedBackpacks.MOD_ID, BACKPACK_REG_NAME), "main");

	public static void registerHandlers(IEventBus modBus) {
		modBus.addListener(ClientEventHandler::onModelRegistry);
		modBus.addListener(ClientEventHandler::registerEntityRenderers);
		modBus.addListener(ClientEventHandler::registerReloadListener);
		modBus.addListener(BackpackTintSources::register);
		modBus.addListener(ModBlockColors::registerBlockColorHandlers);
		modBus.addListener(ClientEventHandler::registerBackpackEntityRenderStateModifier);
		modBus.addListener(ClientEventHandler::registerBackpackItemModels);
		modBus.addListener(ClientEventHandler::registerBlockStateModels);
		BackpackShapes.setShapeProvider(ClientBackpackShapeProvider.INSTANCE);
		IEventBus eventBus = ScNeoForge.EVENT_BUS;
		eventBus.addListener(ClientBackpackContentsTooltip::onWorldLoad);
		eventBus.addListener(ClientEventHandler::handleBlockPick);
		eventBus.addListener(ClientEventHandler::onPlayerLoggingIn);
		eventBus.addListener(ClientEventHandler::onPlayerLoggingOut);
		eventBus.addListener(ClientEventHandler::submitCustomGeometry);
		eventBus.addListener(BackpackStorage::onClientWorldLoad);
		eventBus.addListener(ClientEventHandler::onEntityTick);
		eventBus.addListener(BackpackTemplateStorage::onClientWorldLoad);
	}

	private static void registerBlockStateModels(RegisterBlockStateModels event) {
		event.registerModel(BackpackBlockModel.UnbakedBlockStateModel.ID, BackpackBlockModel.UnbakedBlockStateModel.CODEC);
	}

	private static void registerBackpackItemModels(RegisterItemModelsEvent event) {
		event.register(SophisticatedBackpacks.getIdentifier("backpack"), BackpackItemModel.Unbaked.MAP_CODEC);
	}

	// crouch 时 25 度旋转的四元数常量（粒子回调每次发射都调用，避免重复分配）
	private static final org.joml.Quaternionf XP_ROTATION_CROUCH_25 = Axis.XP.rotationDegrees(25);

	private static void onEntityTick(EntityTickEvent.Post event) {
		Entity entity = event.getEntity();
		// 性能：升级客户端 tick 本就以 1/32 概率节流，将节流检查提前到装备槽扫描之前，
		// 避免每玩家/每实体每 tick 的 getBackpackFromRendered 全槽位扫描与 wrapper 查询。
		// 用确定性取模替代 Random.nextInt：消除每实体每 tick 的 RNG 调用，且触发时机可预测。
		if (Minecraft.getInstance().isPaused() || entity.tickCount % 32 != 0) {
			return;
		}
		if (entity instanceof Player player) {
			PlayerInventoryProvider.get().getBackpackFromRendered(player, false).ifPresent(backpackRenderData -> {
				ItemStack backpack = backpackRenderData.getBackpack();
				IBackpackWrapper wrapper = BackpackWrapper.fromStack(backpack);
				clientTickUpgrades(player, wrapper.getRenderDataHandler());
			});
		} else if (entity instanceof LivingEntity livingEntity) {
			ItemStack chestStack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
			if (chestStack.getItem() instanceof BackpackItem) {
				IBackpackWrapper wrapper = BackpackWrapper.fromStack(chestStack);
				clientTickUpgrades(livingEntity, wrapper.getRenderDataHandler());
			}
		}
	}

	private static void clientTickUpgrades(LivingEntity livingEntity, RenderDataHandler renderDataHandler) {
		renderDataHandler.getUpgradeClientData().forEach((type, data) -> UpgradeClientRegistry.getUpgradeClientTickHandler(type)
				.ifPresent(renderer -> renderUpgrade(renderer, livingEntity, type, data)));
	}

	private static Vector3f getBackpackMiddleFacePoint(LivingEntity livingEntity, Vector3f vector) {
		Vector3f point = new Vector3f(vector);
		boolean isCrouching = livingEntity.isCrouching();
		if (isCrouching) {
			point.rotate(XP_ROTATION_CROUCH_25);
		}
		point.add(0, 0.8f, isCrouching ? 0.9f : 0.7f);
		point.rotate(Axis.YN.rotationDegrees(livingEntity.yBodyRot - 180));
		point.add(livingEntity.position().toVector3f());
		return point;
	}

	private static <T extends IUpgradeClientData> void renderUpgrade(IUpgradeClientTickHandler<T> renderer, LivingEntity livingEntity,
			UpgradeClientDataType<?> type, IUpgradeClientData data) {
		// noinspection unchecked
		type.cast(data).ifPresent(clientData -> renderer.onClientTick(livingEntity.level(), livingEntity.level().getRandom(),
				vector3d -> getBackpackMiddleFacePoint(livingEntity, vector3d), (T) clientData));
	}

	private static void registerBackpackEntityRenderStateModifier(RegisterRenderStateModifiersEvent event) {
		// 全局修改器：提取阶段把穿戴/手持背包数据写入 LivingEntityRenderState，
		// 由注册到各个 LivingEntityRenderer 上的 BackpackLayerRenderer 读取渲染。
		// Fabric 端通过 LivingEntityRenderLayerRegistrationCallback 注册渲染层（见 FabricSophisticatedBackpacksClient）。
		event.registerEntityModifier(null, BackpackLayerRenderer.RENDER_STATE_MODIFIER);
	}

	private static void onPlayerLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
		MobCatcherCaptureEffectRenderer.clear();
		ClientPacketDistributor.sendToServer(new RequestPlayerSettingsPayload());
	}

	private static void onPlayerLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		MobCatcherCaptureEffectRenderer.clear();
	}

	private static void submitCustomGeometry(SubmitCustomGeometryEvent event) {
		// TODO: Fabric port - getLevelRenderState() returns Object in shim, cameraRenderState not accessible
	}

	private static void onModelRegistry(ModelEvent.RegisterLoaders event) {
		event.register(Identifier.fromNamespaceAndPath(SophisticatedBackpacks.MOD_ID, BACKPACK_REG_NAME), BackpackBlockModel.Loader.INSTANCE);
	}

	public static void registerReloadListener(AddClientReloadListenersEvent event) {
		event.addListener(SophisticatedBackpacks.getIdentifier("backpack_layer_registration"), (ResourceManagerReloadListener) resourceManager -> {
			registerBackpackLayer(resourceManager);
			BackpackShapes.reloadDefaultShapeProvider(resourceManager);
			ClientBackpackShapeProvider.INSTANCE.rebuildShapes();
			BackpackShapes.setShapeProvider(ClientBackpackShapeProvider.INSTANCE);
		});
	}

	private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(EVERLASTING_BACKPACK_ITEM_ENTITY.get(), ItemEntityRenderer::new);
		BlockEntityRendererProvider<BackpackBlockEntity, BackpackBlockEntityRenderer.BackpackRenderState> berProvider = BackpackBlockEntityRenderer::new;
		event.registerBlockEntityRenderer(ModBlocks.BACKPACK_TILE_TYPE.get(), berProvider);
	}

	@SuppressWarnings("java:S3740") // explanation below
	private static void registerBackpackLayer(ResourceManager resourceManager) {
		// 渲染层已改由 Fabric API 的 LivingEntityRenderLayerRegistrationCallback 注册
		//（见 FabricSophisticatedBackpacksClient#onInitializeClient），无需再在资源重载时手动挂载。
	}

	public static void handleBlockPick(InputEvent.InteractionKeyMappingTriggered event) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null || player.isCreative() || !event.isPickBlock() || mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) {
			return;
		}
		HitResult target = mc.hitResult;
		Level level = player.level();
		BlockPos pos = ((BlockHitResult) target).getBlockPos();
		BlockState state = level.getBlockState(pos);

		if (state.isAir()) {
			return;
		}

		ItemStack result = state.getCloneItemStack(level, pos, true);

		if (result.isEmpty() || player.getInventory().findSlotMatchingItem(result) > -1) {
			return;
		}

		ClientPacketDistributor.sendToServer(new BlockPickPayload(result));
	}
}
