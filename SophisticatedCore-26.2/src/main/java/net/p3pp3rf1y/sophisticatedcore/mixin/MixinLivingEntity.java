package net.p3pp3rf1y.sophisticatedcore.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.p3pp3rf1y.sophisticatedcore.compat.ScNeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 桥接两个 NeoForge 事件到 Fabric：
 * <ol>
 * <li>{@link LivingDropsEvent} <- {@link LivingEntity#dropAllDeathLoot(ServerLevel, DamageSource)}</li>
 * <li>{@link EntityTickEvent.Post} <- {@link LivingEntity#tick()} 末尾</li>
 * </ol>
 * <p>
 * SB 的监听器 {@code CommonEventHandler::onLivingDrops} 调用
 * {@code EntityBackpackAdditionHandler.handleBackpackDrop(event)}，将怪物胸甲槽中的
 * 背包作为 {@link ItemEntity} 加入 {@code event.getDrops()} 集合并清空胸甲槽，
 * 防止 vanilla 继续执行 dropAllDeathLoot 时重复掉落。
 * <p>
 * 在 HEAD 处注入：此时胸甲槽中的背包还在，监听器将其加入 drops 并清空槽位；
 * 随后本 mixin 遍历 drops 调用 {@code level.addFreshEntity} 生成实体；
 * vanilla 继续执行 dropAllDeathLoot 时胸甲槽已空，不会重复掉落。
 * <p>
 * tick() 末尾注入 {@link EntityTickEvent.Post}：替代之前在 END_LEVEL_TICK 中遍历
 * level.getAllEntities() 的方式。旧方式每 tick 遍历所有实体（包括掉落物、箭矢等），
 * 性能开销巨大；Mixin 方式只在 LivingEntity 实际被 tick 时触发事件，与 NeoForge
 * 原始行为一致，且避免了遍历非 LivingEntity 的无谓开销。
 */
@Mixin(LivingEntity.class)
public class MixinLivingEntity {

	@Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
	private void sophisticatedcore$fireLivingDropsEvent(ServerLevel level, DamageSource source, CallbackInfo ci) {
		// 无监听者短路（只装 SC 不装 SB 时）：跳过 drops 集合与事件对象的分配
		if (!ScNeoForge.EVENT_BUS.hasListeners(LivingDropsEvent.class)) {
			return;
		}
		LivingEntity self = (LivingEntity) (Object) this;
		Collection<ItemEntity> drops = new ArrayList<>();
		// SB 不使用 lootingLevel / recentlyHit，传 0 / false 即可
		LivingDropsEvent event = new LivingDropsEvent(self, source, drops, 0, false);
		ScNeoForge.EVENT_BUS.post(event);
		for (ItemEntity itemEntity : event.getDrops()) {
			level.addFreshEntity(itemEntity);
		}
	}

	@Inject(method = "tick", at = @At("RETURN"))
	private void sophisticatedcore$fireEntityTickEventPost(CallbackInfo ci) {
		// 无监听者短路：每实体每 tick 分配事件对象在密集实体场景下开销显著
		if (!ScNeoForge.EVENT_BUS.hasListeners(EntityTickEvent.Post.class)) {
			return;
		}
		LivingEntity self = (LivingEntity) (Object) this;
		ScNeoForge.EVENT_BUS.post(new EntityTickEvent.Post(self));
	}
}
