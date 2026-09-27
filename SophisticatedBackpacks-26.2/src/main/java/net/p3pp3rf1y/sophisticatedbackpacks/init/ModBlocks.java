package net.p3pp3rf1y.sophisticatedbackpacks.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.p3pp3rf1y.sophisticatedcore.eventbus.EventPriority;
import net.p3pp3rf1y.sophisticatedcore.eventbus.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.p3pp3rf1y.sophisticatedcore.compat.ScNeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.p3pp3rf1y.sophisticatedbackpacks.SophisticatedBackpacks;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlock;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;

import java.util.Set;
import java.util.function.Supplier;

public class ModBlocks {
	private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(SophisticatedBackpacks.MOD_ID);
	private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE,
			SophisticatedBackpacks.MOD_ID);

	private ModBlocks() {
	}

	public static final Supplier<BackpackBlock> BACKPACK = BLOCKS.registerBlock("backpack", BackpackBlock::new);
	public static final Supplier<BackpackBlock> COPPER_BACKPACK = BLOCKS.registerBlock("copper_backpack", BackpackBlock::new);
	public static final Supplier<BackpackBlock> IRON_BACKPACK = BLOCKS.registerBlock("iron_backpack", BackpackBlock::new);
	public static final Supplier<BackpackBlock> GOLD_BACKPACK = BLOCKS.registerBlock("gold_backpack", BackpackBlock::new);
	public static final Supplier<BackpackBlock> DIAMOND_BACKPACK = BLOCKS.registerBlock("diamond_backpack", BackpackBlock::new);
	public static final Supplier<BackpackBlock> NETHERITE_BACKPACK = BLOCKS.registerBlock("netherite_backpack",
			properties -> new BackpackBlock(1200, properties));

	@SuppressWarnings("ConstantConditions") // no datafixer type needed
	public static final Supplier<BlockEntityType<BackpackBlockEntity>> BACKPACK_TILE_TYPE = BLOCK_ENTITY_TYPES.register("backpack",
			() -> new BlockEntityType<>(BackpackBlockEntity::new, Set.of(BACKPACK.get(), COPPER_BACKPACK.get(), IRON_BACKPACK.get(), GOLD_BACKPACK.get(),
					DIAMOND_BACKPACK.get(), NETHERITE_BACKPACK.get())));

	public static void registerHandlers(IEventBus modBus) {
		BLOCKS.register(modBus);
		BLOCK_ENTITY_TYPES.register(modBus);
		ScNeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, BackpackBlock::playerInteract);
		modBus.addListener(ModBlocks::registerCapabilities);
	}

	private static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.Block.ITEM, BACKPACK_TILE_TYPE.get(),
				(be, dir) -> ((BackpackBlockEntity) be).getExternalItemHandler(dir));
		event.registerBlockEntity(Capabilities.Block.FLUID, BACKPACK_TILE_TYPE.get(),
				(be, dir) -> ((BackpackBlockEntity) be).getExternalFluidHandler(dir));
		event.registerBlockEntity(Capabilities.Block.ENERGY, BACKPACK_TILE_TYPE.get(),
				(be, dir) -> ((BackpackBlockEntity) be).getExternalEnergyHandler(dir));
	}
}
