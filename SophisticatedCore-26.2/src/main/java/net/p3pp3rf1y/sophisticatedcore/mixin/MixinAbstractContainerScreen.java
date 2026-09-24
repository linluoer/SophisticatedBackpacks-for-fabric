package net.p3pp3rf1y.sophisticatedcore.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.p3pp3rf1y.sophisticatedcore.compat.ScNeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

/**
 * 拦截 {@link AbstractContainerScreen} 的多个方法：
 * <ul>
 *   <li>{@code showTooltipWithItemInHand} - 包装 tooltip 工厂函数，支持自定义 tooltip 组件</li>
 *   <li>{@code keyPressed} - 在 HEAD 处触发 {@link ScreenEvent.KeyPressed.Pre}，
 *       如果事件被取消则阻止 vanilla 处理</li>
 *   <li>{@code mouseClicked} - 在 HEAD 处触发 {@link ScreenEvent.MouseButtonPressed.Pre}，
 *       如果事件被取消则阻止 vanilla 处理</li>
 * </ul>
 * <p>
 * vanilla 26.2 的 {@code ClientTooltipComponent.create(TooltipComponent)} 使用
 * pattern matching switch，未知类型会抛出 {@link IllegalArgumentException}。
 */
@Mixin(AbstractContainerScreen.class)
public class MixinAbstractContainerScreen {

	@ModifyArg(
			method = "showTooltipWithItemInHand",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/Optional;map(Ljava/util/function/Function;)Ljava/util/Optional;",
					ordinal = 0
			),
			index = 0
	)
	private Function<TooltipComponent, ClientTooltipComponent> sophisticatedcore$wrapTooltipFactory(
			Function<TooltipComponent, ClientTooltipComponent> original
	) {
		return component -> {
			ClientTooltipComponent custom = RegisterClientTooltipComponentFactoriesEvent.createCustom(component);
			return custom != null ? custom : original.apply(component);
		};
	}

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void sophisticatedcore$fireKeyPressedPre(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		AbstractContainerScreen<?> self = (AbstractContainerScreen<?>) (Object) this;
		ScreenEvent.KeyPressed.Pre neoEvent = new ScreenEvent.KeyPressed.Pre(self, event.key(), event.scancode(), event.modifiers());
		ScNeoForge.EVENT_BUS.post(neoEvent);
		if (neoEvent.isCanceled()) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void sophisticatedcore$fireMouseButtonPressedPre(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
		AbstractContainerScreen<?> self = (AbstractContainerScreen<?>) (Object) this;
		ScreenEvent.MouseButtonPressed.Pre neoEvent = new ScreenEvent.MouseButtonPressed.Pre(self, event.button(), event.x(), event.y());
		ScNeoForge.EVENT_BUS.post(neoEvent);
		if (neoEvent.isCanceled()) {
			cir.setReturnValue(true);
		}
	}
}
