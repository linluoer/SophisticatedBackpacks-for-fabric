package net.neoforged.neoforge.event.entity.living;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * 生物生成完成事件 - 用于修改生成的 Mob 属性（位置、装备等）。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。使用 {@link LevelAccessor} 而非 {@code Level}，
 * 因为区块生成期间 {@code Mob#finalizeSpawn} 收到的 level 是 {@code WorldGenRegion}
 * （实现 {@code ServerLevelAccessor} 但不是 {@code Level}）。
 * SB 监听器将 getLevel() 强转为 {@code ServerLevelAccessor}，因此此处返回
 * {@link LevelAccessor} 即可兼容 WorldGenRegion 和 ServerLevel。
 */
public class FinalizeSpawnEvent extends Event {
	private final Mob entity;
	private final LevelAccessor level;
	private final BlockPos pos;
	private final SpawnReason spawnReason;

	public FinalizeSpawnEvent(Mob entity, LevelAccessor level, BlockPos pos, SpawnReason spawnReason) {
		this.entity = entity;
		this.level = level;
		this.pos = pos;
		this.spawnReason = spawnReason;
	}

	public Mob getEntity() {
		return entity;
	}

	public LevelAccessor getLevel() {
		return level;
	}

	public BlockPos getPos() {
		return pos;
	}

	public SpawnReason getSpawnReason() {
		return spawnReason;
	}

	/**
	 * 生成原因枚举 - 简化 shim，对应 Mojang 的 MobSpawnType/EntitySpawnReason。
	 */
	public enum SpawnReason {
		NATURAL,
		CHUNK_GENERATION,
		STRUCTURE,
		MOB_SUMMONED,
		JOCKEY,
		CONVERSION,
		REINFORCEMENT,
		TRIGGERED,
		BREEDING,
		SPAWN_EGG,
		COMMAND,
		DISPENSER,
		SPAWNER,
		EVENT,
		NETHER_PORTAL,
		LIGHTNING_BOLT,
		PATROL,
		SLIME_SPLIT,
		TRAP,
		BUCKET,
		ENCHANTMENT,
		SCRATCH
	}
}
