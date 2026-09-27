package net.neoforged.neoforge.transfer.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * 玩家物品栏包装器。
 */
public class PlayerInventoryWrapper implements ResourceHandler<ItemResource> {
	/** 盔甲槽索引：脚 36、腿 37、胸 38、头 39。 */
	private static final int[] ARMOR_SLOTS = {
			EquipmentSlot.FEET.getIndex(Inventory.INVENTORY_SIZE),
			EquipmentSlot.LEGS.getIndex(Inventory.INVENTORY_SIZE),
			EquipmentSlot.CHEST.getIndex(Inventory.INVENTORY_SIZE),
			EquipmentSlot.HEAD.getIndex(Inventory.INVENTORY_SIZE),
	};

	private final Player player;

	private PlayerInventoryWrapper(Player player) {
		this.player = player;
	}

	public static PlayerInventoryWrapper of(Player player) {
		return new PlayerInventoryWrapper(player);
	}

	/**
	 * 盔甲槽（脚、腿、胸、头）。
	 * <p>
	 * MC 26.2 的 {@link Inventory} 把装备槽并入统一索引空间：主物品栏 0-35，
	 * 盔甲 36-39（由 {@code EquipmentSlot.getIndex(36)} 决定），副手 40。
	 */
	public ResourceHandler<ItemResource> getArmorSlots() {
		return new SlotSubsetHandler(player, ARMOR_SLOTS);
	}

	/**
	 * 手持槽（主手 + 副手）。
	 * <p>
	 * 主手对应当前选中的快捷栏槽位，随玩家切换而变，因此每次调用重新解析。
	 */
	public ResourceHandler<ItemResource> getHandSlots() {
		return new SlotSubsetHandler(player, new int[] { player.getInventory().getSelectedSlot(), Inventory.SLOT_OFFHAND });
	}

	public ResourceHandler<ItemResource> getMainInventory() {
		return VanillaContainerWrapper.of(player.getInventory());
	}

	public ResourceHandler<ItemResource> getMainSlots() {
		return getMainInventory();
	}

	@Override
	public int size() {
		return player.getInventory().getContainerSize();
	}

	@Override
	public ItemResource getResource(int index) {
		return ItemResource.of(player.getInventory().getItem(index));
	}

	@Override
	public long getAmountAsLong(int index) {
		return player.getInventory().getItem(index).getCount();
	}

	@Override
	public int insert(ItemResource resource, int amount, TransactionContext transaction) {
		ItemStack stack = resource.toStack(amount);
		int before = stack.getCount();
		player.getInventory().add(stack);
		int moved = before - stack.getCount();
		return moved;
	}

	@Override
	public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
		Inventory inv = player.getInventory();
		if (index >= inv.getContainerSize()) {
			return 0;
		}
		ItemStack existing = inv.getItem(index);
		if (existing.isEmpty()) {
			ItemStack toInsert = resource.toStack(amount);
			inv.setItem(index, toInsert);
			return amount;
		} else if (resource.matches(existing) && existing.getCount() < existing.getMaxStackSize()) {
			int canInsert = Math.min(existing.getMaxStackSize() - existing.getCount(), amount);
			existing.grow(canInsert);
			return canInsert;
		}
		return 0;
	}

	@Override
	public int extract(ItemResource resource, int amount, TransactionContext transaction) {
		int moved = 0;
		Inventory inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize() && moved < amount; i++) {
			ItemStack existing = inv.getItem(i);
			if (!existing.isEmpty() && resource.matches(existing)) {
				int canExtract = Math.min(existing.getCount(), amount - moved);
				existing.shrink(canExtract);
				moved += canExtract;
			}
		}
		return moved;
	}

	@Override
	public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
		Inventory inv = player.getInventory();
		if (index >= inv.getContainerSize()) {
			return 0;
		}
		ItemStack existing = inv.getItem(index);
		if (existing.isEmpty() || !resource.matches(existing)) {
			return 0;
		}
		int canExtract = Math.min(existing.getCount(), amount);
		existing.shrink(canExtract);
		return canExtract;
	}

	@Override
	public long getCapacityAsLong(int index, ItemResource resource) {
		return resource.isEmpty() ? 64 : resource.getMaxStackSize();
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return !resource.isEmpty();
	}

	/**
	 * 把玩家物品栏的一组指定槽位投影成连续索引的 handler。
	 * <p>
	 * 经验修补等逻辑按 {@code 0..size()-1} 遍历 handler，需要盔甲/手持这类
	 * 非连续槽位表现为独立的小容器。
	 */
	private static class SlotSubsetHandler implements ResourceHandler<ItemResource> {
		private final Player player;
		private final int[] slots;

		private SlotSubsetHandler(Player player, int[] slots) {
			this.player = player;
			this.slots = slots;
		}

		private Inventory inventory() {
			return player.getInventory();
		}

		@Override
		public int size() {
			return slots.length;
		}

		@Override
		public ItemResource getResource(int index) {
			return ItemResource.of(inventory().getItem(slots[index]));
		}

		@Override
		public long getAmountAsLong(int index) {
			return inventory().getItem(slots[index]).getCount();
		}

		@Override
		public int getAmountAsInt(int index) {
			return inventory().getItem(slots[index]).getCount();
		}

		@Override
		public int insert(ItemResource resource, int amount, TransactionContext transaction) {
			int moved = 0;
			for (int i = 0; i < slots.length && moved < amount; i++) {
				moved += insert(i, resource, amount - moved, transaction);
			}
			return moved;
		}

		@Override
		public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
			Inventory inv = inventory();
			int slot = slots[index];
			ItemStack existing = inv.getItem(slot);
			if (existing.isEmpty()) {
				int canInsert = Math.min(resource.getMaxStackSize(), amount);
				inv.setItem(slot, resource.toStack(canInsert));
				return canInsert;
			}
			if (resource.matches(existing) && existing.getCount() < existing.getMaxStackSize()) {
				int canInsert = Math.min(existing.getMaxStackSize() - existing.getCount(), amount);
				existing.grow(canInsert);
				inv.setItem(slot, existing);
				return canInsert;
			}
			return 0;
		}

		@Override
		public int extract(ItemResource resource, int amount, TransactionContext transaction) {
			int moved = 0;
			for (int i = 0; i < slots.length && moved < amount; i++) {
				moved += extract(i, resource, amount - moved, transaction);
			}
			return moved;
		}

		@Override
		public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
			Inventory inv = inventory();
			int slot = slots[index];
			ItemStack existing = inv.getItem(slot);
			if (existing.isEmpty() || !resource.matches(existing)) {
				return 0;
			}
			int canExtract = Math.min(existing.getCount(), amount);
			existing.shrink(canExtract);
			inv.setItem(slot, existing);
			return canExtract;
		}

		@Override
		public long getCapacityAsLong(int index, ItemResource resource) {
			return resource.isEmpty() ? 64 : resource.getMaxStackSize();
		}

		@Override
		public boolean isValid(int index, ItemResource resource) {
			return !resource.isEmpty();
		}
	}
}
