package net.p3pp3rf1y.sophisticatedcore.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.p3pp3rf1y.sophisticatedcore.compat.ScNeoForge;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 桥接 {@code Entity.hurt(DamageSource, float)} 到 NeoForge 的
 * {@link EntityInvulnerabilityCheckEvent} 事件。
 * <p>
 * MC 26.2 中 vanilla 的 {@code hurtServer(ServerLevel, DamageSource, float)} 在
 * {@link Entity} 中是 abstract 方法（无方法体），无法直接注入；具体实现在
 * {@code LivingEntity} / {@code ItemEntity} 等子类。为覆盖所有实体类型（包括
 * {@code EverlastingBackpackItemEntity} 等非 LivingEntity 子类），选择注入
 * {@code Entity.hurt(DamageSource, float)}：这是所有伤害调用的统一入口（内部
 * 委派给 {@code hurtServer}），即使是 {@code final} 方法，mixin 仍可通过
 * {@code @Inject} 在字节码层面注入。
 * <p>
 * SB 监听器 {@code CommonEventHandler::handleEverlastingInvulnerability} 检查
 * {@code event.getEntity() instanceof EverlastingBackpackItemEntity} 后将
 * invulnerable 设为 true，本 mixin 据此取消 hurt 调用，使永恒背包物品实体免疫所有伤害。
 */
@Mixin(Entity.class)
public class MixinEntityHurt {

	@Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
	private void sophisticatedcore$fireInvulnerabilityCheck(DamageSource source, float damage, CallbackInfo ci) {
		Entity self = (Entity) (Object) this;
		// 仅在服务端触发事件，客户端 hurt 直接委托到 hurtClient 不走伤害逻辑
		if (self.level().isClientSide()) {
			return;
		}
		EntityInvulnerabilityCheckEvent event = new EntityInvulnerabilityCheckEvent(self, source, false);
		ScNeoForge.EVENT_BUS.post(event);
		if (event.isInvulnerable()) {
			ci.cancel();
		}
	}
}
