package net.p3pp3rf1y.sophisticatedbackpacks.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.p3pp3rf1y.sophisticatedcore.eventbus.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.p3pp3rf1y.sophisticatedcore.compat.ScNeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.p3pp3rf1y.sophisticatedbackpacks.SophisticatedBackpacks;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModBlocks;

/**
 * Fabric 入口点类。
 * <p>
 * 原始 {@link SophisticatedBackpacks} 使用 NeoForge 的 {@code @Mod} 注解和构造器注入模式，
 * 在 Fabric 中不会被加载器调用。此类实现 {@link ModInitializer}，在 {@code onInitialize()}
 * 中创建必要的 stub 对象（{@link IEventBus}、{@link Dist}、{@link ModContainer}）并实例化
 * 原始类，从而复用其全部初始化逻辑。
 */
public class FabricSophisticatedBackpacks implements ModInitializer {
	@Override
	public void onInitialize() {
		String version = FabricLoader.getInstance().getModContainer(SophisticatedBackpacks.MOD_ID)
				.map(c -> c.getMetadata().getVersion().toString())
				.orElse("1.0.0");
		ModContainer container = new ModContainer(SophisticatedBackpacks.MOD_ID, version);
		IEventBus modBus = container.getEventBus().orElseGet(ModContainer::createEventBus);
		Dist dist = FMLEnvironment.getDist();
		SophisticatedBackpacks instance = new SophisticatedBackpacks(modBus, dist, container);
		container.setModInstance(instance);
		// 触发网络 payload 注册（构造器中已注册监听器，需要 post 事件才会执行）
		modBus.post(new RegisterPayloadHandlersEvent());
		// 触发升级容器类型注册（ModItems.registerContainers 监听 RegisterEvent 且仅处理 MENU 注册表）
		modBus.post(RegisterEvent.forRegistry(Registries.MENU));
		ItemStorage.SIDED.registerForBlockEntity(BackpackBlockEntity::getFabricItemStorage, ModBlocks.BACKPACK_TILE_TYPE.get());

		// 桥接 Fabric UseEntityCallback → NeoForge PlayerInteractEvent.EntityInteractSpecific
		// 使 MobCatcherHandler 和打开他人背包等功能生效
		UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
			PlayerInteractEvent.EntityInteractSpecific event = new PlayerInteractEvent.EntityInteractSpecific(
					player, hand, entity, hitResult.getLocation().subtract(entity.position()));
			ScNeoForge.EVENT_BUS.post(event);
			if (event.isCanceled()) {
				return event.getCancellationResult();
			}
			return InteractionResult.PASS;
		});
	}
}
