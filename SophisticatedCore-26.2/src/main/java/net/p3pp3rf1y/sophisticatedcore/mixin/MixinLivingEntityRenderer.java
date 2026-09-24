package net.p3pp3rf1y.sophisticatedcore.mixin;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.renderstate.RenderStateModifierRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在 {@link LivingEntityRenderer#extractRenderState} 末尾运行注册的渲染状态修改器，
 * 等效于 NeoForge 的 {@code RegisterRenderStateModifiersEvent#registerEntityModifier} 机制。
 * <p>
 * 修改器（如精妙背包的 {@code BackpackLayerRenderer.RENDER_STATE_MODIFIER}）把实体数据
 * 写入渲染状态，渲染层随后在绘制阶段读取这些数据完成穿戴渲染。
 */
@Mixin(LivingEntityRenderer.class)
public class MixinLivingEntityRenderer {

	@Inject(
			method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
			at = @At("TAIL")
	)
	private void sophisticatedcore$runRenderStateModifiers(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
		RenderStateModifierRegistry.runEntityModifiers(entity, state);
	}
}
