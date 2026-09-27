package net.neoforged.neoforge.event.entity;

import net.minecraft.world.entity.Entity;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * 实体破坏方块事件（mob griefing）。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。
 */
public class EntityMobGriefingEvent extends Event {
	private final Entity entity;
	private boolean canGrief;

	public EntityMobGriefingEvent(Entity entity, boolean canGrief) {
		this.entity = entity;
		this.canGrief = canGrief;
	}

	public Entity getEntity() {
		return entity;
	}

	public boolean canGrief() {
		return canGrief;
	}

	public void setCanGrief(boolean canGrief) {
		this.canGrief = canGrief;
	}
}
