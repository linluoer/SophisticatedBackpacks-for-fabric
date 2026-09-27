package net.p3pp3rf1y.sophisticatedcore.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ServerLevelAccessor;
import net.p3pp3rf1y.sophisticatedcore.compat.ScNeoForge;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 桥接 {@link Mob#finalizeSpawn(ServerLevelAccessor, DifficultyInstance, EntitySpawnReason, SpawnGroupData)}
 * 到 NeoForge 的 {@link FinalizeSpawnEvent} 事件。
 * <p>
 * SB 的监听器 {@code CommonEventHandler::onLivingSpecialSpawn} 检查
 * {@code entity instanceof Monster && monster.getItemBySlot(EquipmentSlot.CHEST).isEmpty()}
 * 后调用 {@code EntityBackpackAdditionHandler.handleBackpackAdditionOnSpawn} 为生成的怪物添加背包。
 * <p>
 * 在 RETURN 处注入，确保 vanilla 的 finalizeSpawn 已完成属性/装备初始化，
 * 然后触发事件让 SB 监听器有机会添加背包。
 * <p>
 * 注意：区块生成期间 {@code level} 是 {@code WorldGenRegion}（不是 {@code Level}），
 * 因此事件使用 {@link net.minecraft.world.level.LevelAccessor} 类型以兼容两种情况。
 * SB 监听器将 getLevel() 强转为 {@link ServerLevelAccessor}，WorldGenRegion 实现该接口。
 */
@Mixin(Mob.class)
public class MixinMob {

	@Inject(
			method = "finalizeSpawn",
			at = @At("RETURN")
	)
	private void sophisticatedcore$fireFinalizeSpawnEvent(
			ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason,
			@Nullable SpawnGroupData groupData, CallbackInfoReturnable<SpawnGroupData> cir
	) {
		Mob self = (Mob) (Object) this;
		BlockPos pos = self.blockPosition();
		// 直接传 ServerLevelAccessor（WorldGenRegion 和 ServerLevel 都实现该接口），
		// 事件内部以 LevelAccessor 存储，避免对 WorldGenRegion 的非法 Level 强转。
		FinalizeSpawnEvent event = new FinalizeSpawnEvent(
				self, level, pos, FinalizeSpawnEvent.SpawnReason.NATURAL);
		ScNeoForge.EVENT_BUS.post(event);
	}
}
