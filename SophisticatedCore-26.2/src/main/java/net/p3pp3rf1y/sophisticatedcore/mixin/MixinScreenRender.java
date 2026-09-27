package net.p3pp3rf1y.sophisticatedcore.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.p3pp3rf1y.sophisticatedcore.compat.ScNeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 桥接 {@link Screen#extractRenderState(GuiGraphicsExtractor, int, int, float)} 到
 * NeoForge 的 {@link ScreenEvent.Render.Post} 事件。
 * <p>
 * MC 26.2 中 vanilla {@code Screen.render} 重命名为 {@code extractRenderState}，
 * 参数类型也由 {@code GuiGraphics} 变为 {@link GuiGraphicsExtractor}（同一类，仅更名）。
 * <p>
 * SB 监听器 {@code ClientEventHandler::onDrawScreen} 检查
 * {@code gui instanceof AbstractContainerScreen && !(gui instanceof CreativeModeInventoryScreen)}
 * 后渲染 stash 收纳袋 tooltip。在 RETURN 处注入确保所有子组件已绘制完成，
 * 监听器绘制的 tooltip 出现在最上层。
 */
@Mixin(Screen.class)
public class MixinScreenRender {

	@Inject(
			method = "extractRenderState",
			at = @At("RETURN")
	)
	private void sophisticatedcore$fireScreenRenderPost(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		Screen self = (Screen) (Object) this;
		ScreenEvent.Render.Post event = new ScreenEvent.Render.Post(self, graphics, mouseX, mouseY);
		ScNeoForge.EVENT_BUS.post(event);
	}
}
