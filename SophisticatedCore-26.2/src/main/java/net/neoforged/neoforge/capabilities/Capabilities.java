package net.neoforged.neoforge.capabilities;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * 能力常量类 - 提供标准能力键。
 * 简化实现：能力查询通过Mixin或直接代码实现，此处仅提供常量。
 */
public final class Capabilities {
	private Capabilities() {
	}

	public static final class Item {
		private Item() {
		}

		public static final ItemCapability<ResourceHandler<ItemResource>, Void> ITEM = ItemCapability.create("item/item");
		public static final ItemCapability<ResourceHandler<FluidResource>, Void> FLUID = ItemCapability.create("item/fluid");
		public static final ItemCapability<EnergyHandler, Void> ENERGY = ItemCapability.create("item/energy");
	}

	public static final class Entity {
		private Entity() {
		}

		public static final EntityCapability<ResourceHandler<ItemResource>, Void> ITEM = EntityCapability.create("entity/item");
		public static final EntityCapability<ResourceHandler<FluidResource>, Void> FLUID = EntityCapability.create("entity/fluid");
		public static final EntityCapability<EnergyHandler, Void> ENERGY = EntityCapability.create("entity/energy");
	}

	public static final class Block {
		private Block() {
		}

		public static final BlockCapability<ResourceHandler<ItemResource>, net.minecraft.core.Direction> ITEM = BlockCapability.create("block/item");
		public static final BlockCapability<ResourceHandler<FluidResource>, net.minecraft.core.Direction> FLUID = BlockCapability.create("block/fluid");
		public static final BlockCapability<EnergyHandler, net.minecraft.core.Direction> ENERGY = BlockCapability.create("block/energy");
	}
}
