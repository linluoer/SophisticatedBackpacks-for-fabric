package net.p3pp3rf1y.sophisticatedcore.mixin;

import net.minecraft.world.entity.monster.Creeper;
import net.p3pp3rf1y.sophisticatedcore.compat.ScNeoForge;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 桥接苦力怕爆炸到 NeoForge 的 {@link EntityMobGriefingEvent} 事件。
 * <p>
 * SB 监听器 {@code CommonEventHandler::onEntityMobGriefing} 检查
 * {@code event.getEntity() instanceof Creeper} 后调用
 * {@code EntityBackpackAdditionHandler.removeBeneficialEffects(creeper)}，
 * 在苦力怕爆炸前移除其身上由背包带来的增益效果。
 * 监听器不修改 {@code canGrief}，因此本 mixin 不需要根据返回值改变 vanilla 行为。
 * <p>
 * 注入点为 {@link Creeper} 的 {@code explodeCreeper()} 私有方法 HEAD，
 * 这是苦力怕爆炸的唯一入口。
 */
@Mixin(Creeper.class)
public class MixinCreeper {

	@Inject(method = "explodeCreeper", at = @At("HEAD"))
	private void sophisticatedcore$fireMobGriefingEvent(CallbackInfo ci) {
		Creeper self = (Creeper) (Object) this;
		// canGrief 初始值为 true，SB 监听器不修改此值，仅监听爆炸时机
		EntityMobGriefingEvent event = new EntityMobGriefingEvent(self, true);
		ScNeoForge.EVENT_BUS.post(event);
	}
}
