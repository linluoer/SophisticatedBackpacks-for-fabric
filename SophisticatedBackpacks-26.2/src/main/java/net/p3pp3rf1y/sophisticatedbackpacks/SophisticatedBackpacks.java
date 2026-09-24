package net.p3pp3rf1y.sophisticatedbackpacks;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.p3pp3rf1y.sophisticatedcore.eventbus.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.p3pp3rf1y.sophisticatedcore.compat.ScNeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackShapeReloadListener;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.DatapackBackpackTemplateManager;
import net.p3pp3rf1y.sophisticatedbackpacks.client.ClientEventHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.client.KeybindHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.client.render.ClientBackpackContentsTooltip;
import net.p3pp3rf1y.sophisticatedbackpacks.client.render.ClientMobCatcherHealthTooltip;
import net.p3pp3rf1y.sophisticatedbackpacks.command.BackpackCommand;
import net.p3pp3rf1y.sophisticatedbackpacks.common.CommonEventHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.data.DataGenerators;
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModCompat;
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems;
import net.p3pp3rf1y.sophisticatedbackpacks.registry.RegistryLoader;
import net.p3pp3rf1y.sophisticatedbackpacks.upgrades.mobcatcher.MobCatcherContentsData;
import net.p3pp3rf1y.sophisticatedbackpacks.upgrades.mobcatcher.MobCatcherHealthTooltip;
import net.p3pp3rf1y.sophisticatedbackpacks.upgrades.mobcatcher.MobCatcherStorage;
import net.p3pp3rf1y.sophisticatedcore.inventory.ContainerContents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SophisticatedBackpacks.MOD_ID)
public class SophisticatedBackpacks {
	public static final String MOD_ID = "sophisticatedbackpacks";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	private static String networkProtocolVersion;

	private final RegistryLoader registryLoader = new RegistryLoader();
	public final CommonEventHandler commonEventHandler = new CommonEventHandler();

	@SuppressWarnings("java:S1118") // needs to be public for mod to work
	public SophisticatedBackpacks(IEventBus modBus, Dist dist, ModContainer container) {
		ContainerContents.SettingsCategoryDataRegistry.register(MobCatcherContentsData.CODEC, MobCatcherContentsData.STREAM_CODEC,
				MobCatcherStorage.CAPTURED_MOBS_TAG);
		networkProtocolVersion = container.getModInfo().getVersion().toString();
		container.registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);
		container.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
		// registerExtensionPoint 是 no-op，且 ConfigurationScreen::new 方法引用会触发类加载冲突
		// （整合包中 ForgeConfigAPIPort/architectury 等也提供 ConfigurationScreen 类，构造函数不兼容）
		// 所以移除此调用
		commonEventHandler.registerHandlers(modBus);
		ModCompat.register();
		if (dist == Dist.CLIENT) {
			ClientEventHandler.registerHandlers(modBus);
			modBus.addListener(KeybindHandler::registerKeyMappings);
			modBus.addListener(SophisticatedBackpacks::registerTooltipComponent);
		}

		modBus.addListener(SophisticatedBackpacks::setup);
		modBus.addListener(DataGenerators::gatherData);
		Config.SERVER.initListeners(modBus);
		modBus.addListener(SophisticatedBackpacks::clientSetup);
		BackpackCommand.init(modBus);

		IEventBus eventBus = ScNeoForge.EVENT_BUS;
		eventBus.addListener(this::onAddReloadListener);
	}

	private static void setup(FMLCommonSetupEvent event) {
		event.enqueueWork(ModItems::registerDispenseBehavior);
		event.enqueueWork(ModItems::registerCauldronInteractions);
	}

	private static void clientSetup(FMLClientSetupEvent event) {
		KeybindHandler.register();
	}

	private static void registerTooltipComponent(RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(BackpackItem.BackpackContentsTooltip.class, ClientBackpackContentsTooltip::new);
		event.register(MobCatcherHealthTooltip.class, ClientMobCatcherHealthTooltip::new);
	}

	private void onAddReloadListener(AddServerReloadListenersEvent event) {
		event.addListener(RegistryLoader.KEY, registryLoader);
		event.addListener(DatapackBackpackTemplateManager.Loader.KEY, DatapackBackpackTemplateManager.Loader.INSTANCE);
		event.addListener(BackpackShapeReloadListener.KEY, BackpackShapeReloadListener.INSTANCE);
	}

	public static Identifier getIdentifier(String regName) {
		return Identifier.fromNamespaceAndPath(MOD_ID, regName);
	}

	public static String getRegistryName(String regName) {
		return MOD_ID + ":" + regName;
	}

	public static String getNetworkProtocolVersion() {
		return networkProtocolVersion;
	}
}
