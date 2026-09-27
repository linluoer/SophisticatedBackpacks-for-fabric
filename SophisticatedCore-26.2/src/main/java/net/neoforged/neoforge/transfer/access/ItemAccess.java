package net.neoforged.neoforge.transfer.access;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.VanillaBucketFluidHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

/**
 * 物品访问接口 - 提供对单个物品槽位的访问。
 */
public interface ItemAccess {
	ItemResource getResource();

	int getAmount();

	ItemStack getStack();

	int insert(ItemResource resource, int amount, TransactionContext transaction);

	int extract(ItemResource resource, int amount, TransactionContext transaction);

	/**
	 * 用新资源替换当前槽位中的资源（提取当前资源后插入新资源）。
	 */
	default int exchange(ItemResource resource, int amount, TransactionContext transaction) {
		extract(getResource(), amount, transaction);
		return insert(resource, amount, transaction);
	}

	/**
	 * 从ItemStack创建ItemAccess
	 */
	static ItemAccess forStack(ItemStack stack) {
		return new net.neoforged.neoforge.transfer.access.impl.MutableStackItemAccess(stack);
	}

	/**
	 * 从ResourceHandler和索引创建ItemAccess
	 */
	static ItemAccess forHandlerIndex(net.neoforged.neoforge.transfer.ResourceHandler<ItemResource> handler, int slot) {
		return new net.neoforged.neoforge.transfer.access.impl.HandlerIndexItemAccess(handler, slot);
	}

	/**
	 * 获取能力 - 默认实现：为桶类物品提供 FluidResource 处理器。
	 * 子类可以覆盖此方法以提供更多能力。
	 */
	@SuppressWarnings("unchecked")
	default <C> C getCapability(ItemCapability<C, Void> capability) {
		if (capability == Capabilities.Item.FLUID) {
			return (C) VanillaBucketFluidHandler.of(this).orElse(null);
		}
		return null;
	}
}
