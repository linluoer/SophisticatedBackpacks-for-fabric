package net.neoforged.neoforge.event.tick;

import net.minecraft.world.entity.Entity;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * 实体 tick 事件基类。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。
 */
public abstract class EntityTickEvent extends Event {
	private final Entity entity;

	protected EntityTickEvent(Entity entity) {
		this.entity = entity;
	}

	public Entity getEntity() {
		return entity;
	}

	/**
	 * 实体 tick 前事件。
	 */
	public static class Pre extends EntityTickEvent {
		public Pre(Entity entity) {
			super(entity);
		}
	}

	/**
	 * 实体 tick 后事件。
	 */
	public static class Post extends EntityTickEvent {
		public Post(Entity entity) {
			super(entity);
		}
	}
}
