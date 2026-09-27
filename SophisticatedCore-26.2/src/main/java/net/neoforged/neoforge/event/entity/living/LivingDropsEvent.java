package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

import java.util.Collection;

/**
 * 生物死亡掉落事件。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。
 */
public class LivingDropsEvent extends Event {
	private final LivingEntity entity;
	private final DamageSource source;
	private final Collection<ItemEntity> drops;
	private final int lootingLevel;
	private final boolean recentlyHit;

	public LivingDropsEvent(LivingEntity entity, DamageSource source, Collection<ItemEntity> drops, int lootingLevel, boolean recentlyHit) {
		this.entity = entity;
		this.source = source;
		this.drops = drops;
		this.lootingLevel = lootingLevel;
		this.recentlyHit = recentlyHit;
	}

	public LivingEntity getEntity() {
		return entity;
	}

	public DamageSource getSource() {
		return source;
	}

	public Collection<ItemEntity> getDrops() {
		return drops;
	}

	public int getLootingLevel() {
		return lootingLevel;
	}

	public boolean isRecentlyHit() {
		return recentlyHit;
	}
}
