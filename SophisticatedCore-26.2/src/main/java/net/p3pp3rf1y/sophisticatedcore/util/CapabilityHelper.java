package net.p3pp3rf1y.sophisticatedcore.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public class CapabilityHelper {

	public static void runOnItemHandler(Entity entity, Consumer<ResourceHandler<ItemResource>> run) {
		ResourceHandler<ItemResource> handler = getEntityItemHandler(entity);
		if (handler != null) {
			run.accept(handler);
		}
	}

	public static <T> T getFromItemHandler(Entity entity, Function<ResourceHandler<ItemResource>, T> get, T defaultValue) {
		ResourceHandler<ItemResource> handler = getEntityItemHandler(entity);
		if (handler != null) {
			return get.apply(handler);
		}
		return defaultValue;
	}

	@Nullable
	private static ResourceHandler<ItemResource> getEntityItemHandler(Entity entity) {
		if (entity instanceof Player player) {
			return VanillaContainerWrapper.of(player.getInventory());
		}
		return null;
	}

	public static <T> T getFromItemHandler(Level level, BlockPos pos, @Nullable Direction context, Function<ResourceHandler<ItemResource>, T> get,
			T defaultValue) {
		Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, context);
		if (storage != null) {
			return get.apply(VanillaContainerWrapper.of(storage));
		}
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof Container container) {
			return get.apply(VanillaContainerWrapper.of(container, context));
		}
		return defaultValue;
	}

	public static <T> T getFromItemHandler(Level level, BlockPos pos, Function<ResourceHandler<ItemResource>, T> get, T defaultValue) {
		return getFromItemHandler(level, pos, null, get, defaultValue);
	}

	@SuppressWarnings("unchecked")
	public static <T, C> void runOnCapability(Entity entity, EntityCapability<T, C> capability, @Nullable C context, Consumer<T> run) {
		T t = (T) getEntityCapability(entity, capability, context);
		if (t != null) {
			run.accept(t);
		}
	}

	public static <T> void runOnCapability(ItemAccess itemAccess, ItemCapability<T, Void> capability, Consumer<T> run) {
		T t = itemAccess.getCapability(capability);
		if (t != null) {
			run.accept(t);
		}
	}

	@Nullable
	private static Object getEntityCapability(Entity entity, EntityCapability<?, ?> capability, @Nullable Object context) {
		if (capability == Capabilities.Entity.ITEM && entity instanceof Player player) {
			return VanillaContainerWrapper.of(player.getInventory());
		}
		return null;
	}

	public static <T, U> U getFromCapability(ItemAccess itemAccess, ItemCapability<T, Void> capability, Function<T, U> get, U defaultValue) {
		T t = itemAccess.getCapability(capability);
		if (t == null) {
			return defaultValue;
		}
		return get.apply(t);
	}

	public static <T, C, U> U getFromCapability(Level level, BlockPos pos, BlockCapability<T, C> capability, @Nullable C context, Function<T, U> get,
			U defaultValue) {
		return defaultValue;
	}

	public static <T, C, U> U getFromCapability(BlockEntity blockEntity, BlockCapability<T, C> capability, @Nullable C context, Function<T, U> get,
			U defaultValue) {
		return defaultValue;
	}

	public static <T, C, U> U getFromCapability(Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity,
			BlockCapability<T, C> capability, @Nullable C context, Function<T, U> get, U defaultValue) {
		return defaultValue;
	}

	@SuppressWarnings("unchecked")
	public static <T, C, U> U getFromCapability(Entity entity, EntityCapability<T, C> capability, @Nullable C context, Function<T, U> get, U defaultValue) {
		T t = (T) getEntityCapability(entity, capability, context);
		if (t == null) {
			return defaultValue;
		}
		return get.apply(t);
	}

	public static <T> T getFromFluidHandler(BlockEntity be, Direction side, Function<ResourceHandler<FluidResource>, T> get, T defaultValue) {
		return defaultValue;
	}

	public static <T> T getFromFluidHandler(ItemAccess itemAccess, Function<ResourceHandler<FluidResource>, T> get, T defaultValue) {
		return getFromCapability(itemAccess, Capabilities.Item.FLUID, get, defaultValue);
	}
}
