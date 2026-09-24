package net.neoforged.neoforge.transfer.item;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import org.jetbrains.annotations.Nullable;

/**
 * 基于ResourceHandler的Slot实现。
 */
public class ResourceHandlerSlot extends Slot {
	private static final Container EMPTY_CONTAINER = new SimpleContainer(0);
	protected final ResourceHandler<ItemResource> handler;
	protected final IndexModifier<ItemResource> setter;
	protected final int slot;

	public ResourceHandlerSlot(ResourceHandler<ItemResource> handler, IndexModifier<ItemResource> setter, int slot, int x, int y) {
		super(EMPTY_CONTAINER, slot, x, y);
		this.handler = handler;
		this.setter = setter;
		this.slot = slot;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return handler.isValid(slot, ItemResource.of(stack));
	}

	@Override
	public ItemStack getItem() {
		return handler.getResource(slot).toStack(handler.getAmountAsInt(slot));
	}

	@Override
	public void set(ItemStack stack) {
		setter.set(slot, ItemResource.of(stack), stack.getCount());
		setChanged();
	}

	@Override
	public void setChanged() {
		// no-op
	}

	@Override
	public ItemStack remove(int amount) {
		ItemStack stack = getItem();
		ItemStack ret = stack.split(amount);
		set(stack);
		return ret;
	}

	@Override
	public int getMaxStackSize() {
		return handler.getCapacityAsInt(slot, ItemResource.EMPTY);
	}

	@Override
	public int getMaxStackSize(ItemStack stack) {
		return handler.getCapacityAsInt(slot, ItemResource.of(stack));
	}

	@Override
	public boolean mayPickup(Player playerIn) {
		return true;
	}

	@Override
	public int getContainerSlot() {
		return slot;
	}

	public boolean isSameInventory(Slot other) {
		return other instanceof ResourceHandlerSlot rhs && rhs.handler == this.handler;
	}

	@Nullable
	@Override
	public net.minecraft.resources.Identifier getNoItemIcon() {
		return null;
	}
}
