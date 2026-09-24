package net.p3pp3rf1y.sophisticatedcore.mixin;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 拦截 {@link ClientTooltipComponent#create(TooltipComponent)} 静态方法，
 * 在 vanilla 的 pattern matching switch 抛出 IllegalArgumentException 之前，
 * 检查自定义 tooltip 组件工厂。
 * <p>
 * vanilla 26.2 的 create(TooltipComponent) 使用 switch 表达式，
 * 未知类型会抛出 IllegalArgumentException，导致所有调用此方法的地方都会崩溃。
 * 此 Mixin 在 HEAD 处注入，如果有自定义工厂则直接返回，否则让 vanilla 继续处理。
 * <p>
 * 注意：目标是接口，因此 Mixin 必须是接口，且 handler 方法必须为 private static。
 */
@Mixin(ClientTooltipComponent.class)
public interface MixinClientTooltipComponent {

	@Inject(
			method = "create(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;",
			at = @At("HEAD"),
			cancellable = true
	)
	private static void sophisticatedcore$onCreateTooltipComponent(
			TooltipComponent component, CallbackInfoReturnable<ClientTooltipComponent> cir) {
		ClientTooltipComponent custom = RegisterClientTooltipComponentFactoriesEvent.createCustom(component);
		if (custom != null) {
			cir.setReturnValue(custom);
		}
	}
}
