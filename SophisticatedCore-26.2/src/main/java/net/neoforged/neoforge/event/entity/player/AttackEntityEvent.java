package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * 玩家攻击实体事件。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。
 */
public class AttackEntityEvent extends Event {
	private final Player player;
	private final Entity target;

	public AttackEntityEvent(Player player, Entity target) {
		this.player = player;
		this.target = target;
	}

	public Player getEntity() {
		return player;
	}

	public Player getPlayer() {
		return player;
	}

	public Entity getTarget() {
		return target;
	}
}
