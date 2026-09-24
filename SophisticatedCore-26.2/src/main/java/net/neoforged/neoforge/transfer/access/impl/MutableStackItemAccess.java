package net.neoforged.neoforge.transfer.access.impl;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * 基于可变ItemStack的ItemAccess实现。
 */
public class MutableStackItemAccess implements ItemAccess {
	private final ResourceHandler<ItemResource> wrapper;
	private final SimpleContainer container;

	public MutableStackItemAccess(ItemStack stack) {
		this.container = new SimpleContainer(stack) {
			@Override
			public void setItem(int slot, ItemStack stack) {
				getItems().set(slot, stack);
			}
		};
		this.wrapper = VanillaContainerWrapper.of(container);
	}

	@Override
	public ItemResource getResource() {
		return wrapper.getResource(0);
	}

	@Override
	public int getAmount() {
		return wrapper.getAmountAsInt(0);
	}

	@Override
	public ItemStack getStack() {
		return container.getItem(0);
	}

	@Override
	public int insert(ItemResource resource, int amount, TransactionContext transaction) {
		return wrapper.insert(resource, amount, transaction);
	}

	@Override
	public int extract(ItemResource resource, int amount, TransactionContext transaction) {
		return wrapper.extract(resource, amount, transaction);
	}
}
