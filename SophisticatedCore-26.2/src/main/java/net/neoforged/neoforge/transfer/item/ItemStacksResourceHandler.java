package net.neoforged.neoforge.transfer.item;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;

/**
 * 基于NonNullList<ItemStack>的物品资源处理器抽象基类。
 * 管理物品列表并提供ResourceHandler<ItemResource>的全部实现。
 * <p>
 * {@link #insert} 和 {@link #extract} 方法支持事务回滚：在修改槽位前保存原始堆栈副本，
 * 并通过 {@link TransactionContext#addCloseCallback} 注册回滚回调。
 * 当事务未提交而关闭时，回滚回调按注册逆序恢复原始堆栈。
 */
public abstract class ItemStacksResourceHandler implements ResourceHandler<ItemResource> {
	protected NonNullList<ItemStack> stacks;
	protected Codec<List<ItemStack>> codec = Codec.list(ItemStack.CODEC);

	protected ItemStacksResourceHandler(int size) {
		this.stacks = NonNullList.withSize(size, ItemStack.EMPTY);
	}

	protected void onContentsChanged(int index, ItemStack previousContents) {
		// 默认no-op，子类可重写
	}

	public void set(int index, ItemResource resource, int amount) {
		ItemStack previousContents = stacks.get(index);
		if (amount <= 0 || resource.isEmpty()) {
			stacks.set(index, ItemStack.EMPTY);
		} else {
			stacks.set(index, resource.toStack(amount));
		}
		onContentsChanged(index, previousContents);
	}

	public ItemStack getStackInSlot(int slot) {
		return stacks.get(slot);
	}

	@Override
	public int size() {
		return stacks.size();
	}

	@Override
	public ItemResource getResource(int index) {
		return ItemResource.of(stacks.get(index));
	}

	@Override
	public long getAmountAsLong(int index) {
		return stacks.get(index).getCount();
	}

	@Override
	public int getAmountAsInt(int index) {
		return stacks.get(index).getCount();
	}

	@Override
	public int insert(ItemResource resource, int amount, TransactionContext transaction) {
		int moved = 0;
		// 先尝试合并到已有的相同物品
		for (int i = 0; i < stacks.size() && moved < amount; i++) {
			ItemStack existing = stacks.get(i);
			if (!existing.isEmpty() && resource.matches(existing)) {
				int cap = getCapacity(i, resource);
				int canInsert = Math.min(cap - existing.getCount(), amount - moved);
				if (canInsert > 0) {
					ItemStack original = existing.copy();
					existing.grow(canInsert);
					moved += canInsert;
					final int slotIndex = i;
					transaction.addCloseCallback(() -> restore(slotIndex, original));
					onContentsChanged(i, original);
				}
			}
		}
		// 再尝试放入空槽
		for (int i = 0; i < stacks.size() && moved < amount; i++) {
			ItemStack existing = stacks.get(i);
			if (existing.isEmpty() && isValid(i, resource)) {
				int cap = getCapacity(i, resource);
				int canInsert = Math.min(cap, amount - moved);
				if (canInsert > 0) {
					stacks.set(i, resource.toStack(canInsert));
					moved += canInsert;
					final int slotIndex = i;
					transaction.addCloseCallback(() -> restore(slotIndex, ItemStack.EMPTY));
					onContentsChanged(i, ItemStack.EMPTY);
				}
			}
		}
		return moved;
	}

	@Override
	public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
		if (!isValid(index, resource)) {
			return 0;
		}
		ItemStack existing = stacks.get(index);
		int cap = getCapacity(index, resource);
		int canInsert;
		if (existing.isEmpty()) {
			canInsert = Math.min(cap, amount);
			if (canInsert > 0) {
				stacks.set(index, resource.toStack(canInsert));
				transaction.addCloseCallback(() -> restore(index, ItemStack.EMPTY));
				onContentsChanged(index, ItemStack.EMPTY);
			}
		} else if (resource.matches(existing)) {
			canInsert = Math.min(cap - existing.getCount(), amount);
			if (canInsert > 0) {
				ItemStack original = existing.copy();
				existing.grow(canInsert);
				transaction.addCloseCallback(() -> restore(index, original));
				onContentsChanged(index, original);
			}
		} else {
			canInsert = 0;
		}
		return canInsert;
	}

	@Override
	public int extract(ItemResource resource, int amount, TransactionContext transaction) {
		int moved = 0;
		for (int i = 0; i < stacks.size() && moved < amount; i++) {
			ItemStack existing = stacks.get(i);
			if (!existing.isEmpty() && resource.matches(existing)) {
				int canExtract = Math.min(existing.getCount(), amount - moved);
				if (canExtract > 0) {
					ItemStack original = existing.copy();
					existing.shrink(canExtract);
					moved += canExtract;
					final int slotIndex = i;
					transaction.addCloseCallback(() -> restore(slotIndex, original));
					onContentsChanged(i, original);
				}
			}
		}
		return moved;
	}

	@Override
	public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
		ItemStack existing = stacks.get(index);
		if (existing.isEmpty() || !resource.matches(existing)) {
			return 0;
		}
		int canExtract = Math.min(existing.getCount(), amount);
		if (canExtract > 0) {
			ItemStack original = existing.copy();
			existing.shrink(canExtract);
			transaction.addCloseCallback(() -> restore(index, original));
			onContentsChanged(index, original);
		}
		return canExtract;
	}

	@Override
	public long getCapacityAsLong(int index, ItemResource resource) {
		return getCapacity(index, resource);
	}

	protected int getCapacity(int index, ItemResource resource) {
		return resource.isEmpty() ? 64 : resource.getMaxStackSize();
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return !resource.isEmpty();
	}

	public NonNullList<ItemStack> getStacks() {
		return stacks;
	}

	private void restore(int index, ItemStack original) {
		ItemStack current = stacks.get(index);
		stacks.set(index, original);
		onContentsChanged(index, current);
	}

	public void deserialize(ValueInput input) {
		input.read("stacks", this.codec).ifPresent(list -> {
			NonNullList<ItemStack> newStacks = NonNullList.withSize(Math.max(stacks.size(), list.size()), ItemStack.EMPTY);
			for (int i = 0; i < list.size() && i < newStacks.size(); i++) {
				newStacks.set(i, list.get(i));
			}
			this.stacks = newStacks;
		});
	}
}
