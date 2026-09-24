package net.neoforged.neoforge.transfer.item;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.Resource;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * 物品资源 - 代表一个物品（不含数量），基于ItemStack（count=1）。
 */
public final class ItemResource implements Resource {
	public static final ItemResource EMPTY = new ItemResource(ItemStack.EMPTY);

	private final ItemStack stack;

	private ItemResource(ItemStack stack) {
		this.stack = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
	}

	public static ItemResource of(ItemStack stack) {
		if (stack.isEmpty()) {
			return EMPTY;
		}
		return new ItemResource(stack);
	}

	public static ItemResource of(Item item) {
		if (item == null || item == net.minecraft.world.item.Items.AIR) {
			return EMPTY;
		}
		return new ItemResource(new ItemStack(item));
	}

	public Item getItem() {
		return stack.getItem();
	}

	public ItemStack toStack() {
		return stack.copy();
	}

	public ItemStack toStack(int count) {
		return stack.copyWithCount(count);
	}

	public boolean matches(ItemStack otherStack) {
		return !otherStack.isEmpty() && ItemStack.isSameItemSameComponents(stack, otherStack);
	}

	@Override
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	public int getMaxStackSize() {
		return stack.getMaxStackSize();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ItemResource that = (ItemResource) o;
		return ItemStack.isSameItemSameComponents(stack, that.stack);
	}

	@Override
	public int hashCode() {
		return ItemStack.hashItemAndComponents(stack);
	}

	@Override
	public String toString() {
		return "ItemResource[" + stack.getItem() + "]";
	}

	// DataComponentHolder compatibility methods

	@Nullable
	public <T> T get(DataComponentType<? extends T> componentType) {
		return stack.get(componentType);
	}

	public <T> T getOrDefault(DataComponentType<? extends T> componentType, T defaultValue) {
		return stack.getOrDefault(componentType, defaultValue);
	}

	public boolean has(DataComponentType<?> componentType) {
		return stack.has(componentType);
	}

	public net.minecraft.core.component.DataComponentMap getComponents() {
		return stack.getComponents();
	}

	public int getDamageValue() {
		return stack.getOrDefault(DataComponents.DAMAGE, 0);
	}

	public boolean is(TagKey<Item> tag) {
		return stack.is(tag);
	}
}
