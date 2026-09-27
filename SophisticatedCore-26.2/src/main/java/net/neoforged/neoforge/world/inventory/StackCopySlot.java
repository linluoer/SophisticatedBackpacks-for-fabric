package net.neoforged.neoforge.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 复制堆栈的Slot - 通过 {@link #getStackCopy()} 和 {@link #setStackCopy(ItemStack)}
 * 与底层 handler 交互。子类必须重写这两个方法以提供实际的数据读写。
 */
public class StackCopySlot extends Slot {
	private static final Container EMPTY_CONTAINER = new SimpleContainer(0);
	protected final int slotIndex;

	public StackCopySlot(int slotIndex, int x, int y) {
		super(EMPTY_CONTAINER, slotIndex, x, y);
		this.slotIndex = slotIndex;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return true;
	}

	@Override
	public ItemStack getItem() {
		return getStackCopy();
	}

	@Override
	public void set(ItemStack stack) {
		setStackCopy(stack);
		setChanged();
	}

	@Override
	public void setChanged() {
		// no-op by default, subclasses can override
	}

	@Override
	public ItemStack remove(int amount) {
		ItemStack stack = getStackCopy();
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack ret = stack.split(amount);
		setStackCopy(stack);
		return ret;
	}

	@Override
	public boolean mayPickup(Player playerIn) {
		return true;
	}

	@Override
	public int getContainerSlot() {
		return slotIndex;
	}

	protected ItemStack getStackCopy() {
		return ItemStack.EMPTY;
	}

	protected void setStackCopy(ItemStack stack) {
		// no-op by default, subclasses must override
	}
}
