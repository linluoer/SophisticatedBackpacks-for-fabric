package net.neoforged.neoforge.transfer.access.impl;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * 基于ResourceHandler+索引的ItemAccess实现。
 */
public class HandlerIndexItemAccess implements ItemAccess {
	private final ResourceHandler<ItemResource> handler;
	private final int slot;

	public HandlerIndexItemAccess(ResourceHandler<ItemResource> handler, int slot) {
		this.handler = handler;
		this.slot = slot;
	}

	@Override
	public ItemResource getResource() {
		return handler.getResource(slot);
	}

	@Override
	public int getAmount() {
		return handler.getAmountAsInt(slot);
	}

	@Override
	public ItemStack getStack() {
		return handler.getResource(slot).toStack(handler.getAmountAsInt(slot));
	}

	@Override
	public int insert(ItemResource resource, int amount, TransactionContext transaction) {
		return handler.insert(slot, resource, amount, transaction);
	}

	@Override
	public int extract(ItemResource resource, int amount, TransactionContext transaction) {
		return handler.extract(slot, resource, amount, transaction);
	}
}
