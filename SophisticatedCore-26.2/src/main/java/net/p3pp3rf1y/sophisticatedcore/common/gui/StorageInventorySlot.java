package net.p3pp3rf1y.sophisticatedcore.common.gui;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.p3pp3rf1y.sophisticatedcore.api.ISlotChangeResponseUpgrade;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;

public class StorageInventorySlot extends SlotSuppliedHandler {
	private final IStorageWrapper storageWrapper;
	private final int slotIndex;
	private final Player player;

	public StorageInventorySlot(IStorageWrapper storageWrapper, int slotIndex, Player player) {
		super(storageWrapper::getInventoryHandler, slotIndex, 0, 0);
		this.storageWrapper = storageWrapper;
		this.slotIndex = slotIndex;
		this.player = player;
	}

	@Override
	public void set(ItemStack stack) {
		super.set(stack);
		if (!player.level().isClientSide()) {
			processSlotChangeResponse(slotIndex, storageWrapper.getInventoryHandler(), storageWrapper);
		}
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return storageWrapper.getInventoryHandler().isItemValid(slotIndex, stack, player);
	}

	@Override
	public boolean mayPickup(Player player) {
		return storageWrapper.getInventoryHandler().isSlotAccessible(slotIndex);
	}

	private static void processSlotChangeResponse(int slot, InventoryHandler handler, IStorageWrapper storageWrapper) {
		storageWrapper.getUpgradeHandler().getWrappersThatImplementFromMainStorage(ISlotChangeResponseUpgrade.class)
				.forEach(u -> u.onSlotChange(handler, slot));
	}

	@Override
	public int getMaxStackSize(ItemStack stack) {
		return storageWrapper.getInventoryHandler().getCapacityAsInt(slotIndex, ItemResource.of(stack));
	}
}
