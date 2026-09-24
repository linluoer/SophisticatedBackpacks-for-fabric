package net.neoforged.neoforge.event.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * 实体离开关卡事件。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。
 */
public class EntityLeaveLevelEvent extends Event {
	private final Entity entity;
	private final Level level;

	public EntityLeaveLevelEvent(Entity entity, Level level) {
		this.entity = entity;
		this.level = level;
	}

	public Entity getEntity() {
		return entity;
	}

	public Level getLevel() {
		return level;
	}
}
