package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * 玩家事件基类。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。
 */
public abstract class PlayerEvent extends Event {
	private final Player player;

	protected PlayerEvent(Player player) {
		this.player = player;
	}

	public Player getEntity() {
		return player;
	}

	public Player getPlayer() {
		return player;
	}

	/**
	 * 玩家登录事件。
	 */
	public static class PlayerLoggedInEvent extends PlayerEvent {
		public PlayerLoggedInEvent(Player player) {
			super(player);
		}
	}

	/**
	 * 玩家登出事件。
	 */
	public static class PlayerLoggedOutEvent extends PlayerEvent {
		public PlayerLoggedOutEvent(Player player) {
			super(player);
		}
	}

	/**
	 * 玩家重生事件。
	 */
	public static class PlayerRespawnEvent extends PlayerEvent {
		private final boolean endConquered;

		public PlayerRespawnEvent(Player player, boolean endConquered) {
			super(player);
			this.endConquered = endConquered;
		}

		public boolean isEndConquered() {
			return endConquered;
		}
	}

	/**
	 * 玩家维度切换事件。
	 */
	public static class PlayerChangedDimensionEvent extends PlayerEvent {
		public PlayerChangedDimensionEvent(Player player) {
			super(player);
		}
	}
}
