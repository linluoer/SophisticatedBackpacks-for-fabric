package net.p3pp3rf1y.sophisticatedcore.common.gui;

import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.p3pp3rf1y.sophisticatedcore.inventory.ISlotStackAccessor;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * 基于 ResourceHandler 的 Slot 实现，行为对齐 vanilla 26.2 Slot。
 * <p>
 * {@link #getItem()} 返回 handler 内的活引用（与 vanilla {@code Slot.getItem()} 一致），
 * 消除原先 ItemResource 往返拷贝带来的每帧每槽 3 次对象分配；仅对非
 * {@link ItemStacksResourceHandler} 系 handler 保留 ItemResource 路径兜底。
 */
public class SlotSuppliedHandler extends Slot {
	private static final Container emptyInventory = new SimpleContainer(0);
	private final Supplier<ResourceHandler<ItemResource>> itemHandlerSupplier;
	public final int slot;
	private @Nullable Identifier backgroundIcon;

	public SlotSuppliedHandler(Supplier<ResourceHandler<ItemResource>> itemHandlerSupplier, int slot, int xPosition, int yPosition) {
		super(emptyInventory, 0, xPosition, yPosition);
		this.itemHandlerSupplier = itemHandlerSupplier;
		this.slot = slot;
	}

	public SlotSuppliedHandler setBackground(Identifier backgroundIcon) {
		this.backgroundIcon = backgroundIcon;
		return this;
	}

	@Override
	public @Nullable Identifier getNoItemIcon() {
		return backgroundIcon;
	}

	public ResourceHandler<ItemResource> getResourceHandler() {
		return itemHandlerSupplier.get();
	}

	@Override
	public void onQuickCraft(ItemStack oldStackIn, ItemStack newStackIn) {
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return itemHandlerSupplier.get().isValid(slot, ItemResource.of(stack));
	}

	@Override
	public ItemStack getItem() {
		ResourceHandler<ItemResource> handler = itemHandlerSupplier.get();
		// 活引用路径：与 vanilla Slot.getItem() 语义一致（零分配）。
		// 调用方（broadcastChanges 存 lastSlots 时、渲染层）需要副本时自行 copy，
		// vanilla doClick 的 split/shrink 就地变更模式也依赖活引用。
		if (handler instanceof ItemStacksResourceHandler stacksHandler) {
			return stacksHandler.getStackInSlot(slot);
		}
		return handler.getResource(slot).toStack(handler.getAmountAsInt(slot));
	}

	private ItemStack getHandlerStack() {
		ResourceHandler<ItemResource> handler = itemHandlerSupplier.get();
		return handler.getResource(slot).toStack(handler.getAmountAsInt(slot));
	}

	/**
	 * 对齐 vanilla Slot.set：写入 handler，然后 setChanged（此处 setChanged 为 no-op）。
	 * 不维护缓存，避免与 vanilla safeInsert/tryRemove 的 getItem 语义冲突。
	 */
	@Override
	public void set(ItemStack stack) {
		setHandlerStack(stack);
	}

	@Override
	public void setChanged() {
		// no-op: handler 已在 set() 中同步更新，无需写回缓存。
	}

	@Override
	public ItemStack remove(int amount) {
		ItemStack stack = getHandlerStack();
		ItemStack ret = stack.split(amount);
		setHandlerStack(stack);
		return ret;
	}

	private void setHandlerStack(ItemStack stack) {
		Object handler = itemHandlerSupplier.get();
		if (handler instanceof ISlotStackAccessor slotStackAccessor) {
			slotStackAccessor.setStackInSlot(slot, stack);
		} else if (handler instanceof IndexModifier<?> indexModifier) {
			// noinspection unchecked
			((IndexModifier<ItemResource>) indexModifier).set(slot, ItemResource.of(stack), stack.getCount());
		} else {
			InventoryHelper.set(itemHandlerSupplier.get(), slot, ItemResource.of(stack), stack.getCount());
		}
	}

	@Override
	public int getMaxStackSize() {
		return itemHandlerSupplier.get().getCapacityAsInt(slot, ItemResource.EMPTY);
	}

	@Override
	public int getContainerSlot() {
		return slot;
	}

	public boolean isSameInventory(Slot other) {
		return other instanceof SlotSuppliedHandler rhs && rhs.itemHandlerSupplier.get() == this.itemHandlerSupplier.get();
	}
}
