package net.neoforged.neoforge.transfer.item;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.transfer.access.ItemAccess;

/**
 * 基于ItemAccess和DataComponent的物品处理器。
 * 管理存储在ItemStack的ItemContainerContents组件中的物品。
 */
public class ItemAccessItemHandler extends ItemStacksResourceHandler {
	protected final ItemAccess itemAccess;
	protected final DataComponentType<ItemContainerContents> component;
	protected final int size;

	public ItemAccessItemHandler(ItemAccess itemAccess, DataComponentType<ItemContainerContents> component, int size) {
		super(size);
		this.itemAccess = itemAccess;
		this.component = component;
		this.size = size;
		ItemContainerContents contents = getContents(itemAccess.getResource());
		contents.copyInto(stacks);
	}

	protected ItemContainerContents getContents(ItemResource resource) {
		ItemStack stack = resource.toStack();
		return stack.getOrDefault(component, ItemContainerContents.EMPTY);
	}

	private static int getContainerSize(ItemContainerContents contents) {
		return (int) contents.allItemsCopyStream().count();
	}

	protected ItemStack getStackFromContents(ItemContainerContents contents, int slot) {
		NonNullList<ItemStack> list = NonNullList.withSize(Math.max(getContainerSize(contents), size), ItemStack.EMPTY);
		contents.copyInto(list);
		return slot < list.size() ? list.get(slot) : ItemStack.EMPTY;
	}

	protected ItemResource update(ItemResource accessResource, int index, ItemResource newResource, int newAmount) {
		ItemContainerContents contents = getContents(accessResource);
		NonNullList<ItemStack> list = NonNullList.withSize(Math.max(getContainerSize(contents), size), ItemStack.EMPTY);
		contents.copyInto(list);
		ItemStack previous = list.get(index);
		if (newAmount <= 0 || newResource.isEmpty()) {
			list.set(index, ItemStack.EMPTY);
		} else {
			list.set(index, newResource.toStack(newAmount));
		}
		ItemStack accessStack = itemAccess.getStack();
		accessStack.set(component, ItemContainerContents.fromItems(list));
		return ItemResource.of(previous);
	}

	@Override
	protected void onContentsChanged(int index, ItemStack previousContents) {
		super.onContentsChanged(index, previousContents);
		update(itemAccess.getResource(), index, ItemResource.of(stacks.get(index)), stacks.get(index).getCount());
	}

	@Override
	protected int getCapacity(int index, ItemResource resource) {
		return resource.isEmpty() ? 64 : Math.min(64, resource.getMaxStackSize());
	}
}
