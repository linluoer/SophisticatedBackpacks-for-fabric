package net.neoforged.neoforge.common.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * 战利品表 ID 条件 shim。
 * <p>
 * 在 NeoForge 中，此条件用于检查当前正在查询的战利品表 ID 是否与目标 ID 匹配。
 * 由于 MC 26.2 中 {@link LootContext} 不再提供 {@code getQueriedLootTableId()} 方法，
 * 此 shim 的 {@link #test(LootContext)} 始终返回 {@code false}（no-op）。
 */
public class LootTableIdCondition implements LootItemCondition {

	private final Identifier targetLootTableId;

	public static final MapCodec<LootTableIdCondition> CODEC = RecordCodecBuilder.mapCodec(
			inst -> inst.group(Identifier.CODEC.fieldOf("loot_table_id").forGetter(c -> c.targetLootTableId))
					.apply(inst, LootTableIdCondition::new));

	public LootTableIdCondition(Identifier targetLootTableId) {
		this.targetLootTableId = targetLootTableId;
	}

	public Identifier getLootTableId() {
		return targetLootTableId;
	}

	@Override
	public MapCodec<? extends LootItemCondition> codec() {
		return CODEC;
	}

	@Override
	public boolean test(LootContext lootContext) {
		// MC 26.2 中 LootContext.getQueriedLootTableId() 已被移除，shim 始终返回 false。
		return false;
	}

	/**
	 * 创建一个针对指定战利品表 ID 的条件构造器。
	 *
	 * @param id 目标战利品表 ID
	 * @return 条件构造器
	 */
	public static Builder builder(Identifier id) {
		return new Builder(id);
	}

	public static class Builder implements LootItemCondition.Builder {
		private final Identifier targetLootTableId;

		public Builder(Identifier targetLootTableId) {
			this.targetLootTableId = targetLootTableId;
		}

		@Override
		public LootItemCondition build() {
			return new LootTableIdCondition(targetLootTableId);
		}
	}
}
