package net.neoforged.neoforge.event.entity.player;

import net.minecraft.util.TriState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * 物品实体拾取事件 shim。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。
 */
public abstract class ItemEntityPickupEvent extends Event {
	private final Player player;
	private final ItemEntity itemEntity;

	protected ItemEntityPickupEvent(Player player, ItemEntity itemEntity) {
		this.player = player;
		this.itemEntity = itemEntity;
	}

	public Player getPlayer() {
		return player;
	}

	public Player getEntity() {
		return player;
	}

	public ItemEntity getItemEntity() {
		return itemEntity;
	}

	/**
	 * 拾取前事件 - 可通过 {@link #setCanPickup(TriState)} 控制拾取行为。
	 */
	public static class Pre extends ItemEntityPickupEvent {
		private TriState canPickup = TriState.DEFAULT;

		public Pre(Player player, ItemEntity itemEntity) {
			super(player, itemEntity);
		}

		public TriState canPickup() {
			return canPickup;
		}

		public void setCanPickup(TriState canPickup) {
			this.canPickup = canPickup;
		}
	}
}
