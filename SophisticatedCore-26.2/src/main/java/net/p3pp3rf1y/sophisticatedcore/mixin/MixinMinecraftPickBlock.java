package net.p3pp3rf1y.sophisticatedcore.mixin;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.InputEvent;
import net.p3pp3rf1y.sophisticatedcore.compat.ScNeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 桥接 {@link Minecraft#pickBlockOrEntity()} 到 NeoForge 的
 * {@link InputEvent.InteractionKeyMappingTriggered} 事件。
 * <p>
 * MC 26.2 中 vanilla {@code Minecraft.pickBlock} 改名为 {@code pickBlockOrEntity}，
 * 在 {@code handleKeybinds} 中由 {@code keyPickItem.consumeClick()} 触发。
 * <p>
 * SB 监听器 {@code ClientEventHandler::handleBlockPick} 通过 {@code event.isPickBlock()}
 * 判断是拾取方块键触发，并检查 {@code !player.isCreative()} 后向服务器发送
 * {@code BlockPickPayload}，为生存模式玩家提供方块拾取能力。
 * 监听器不取消事件，因此 vanilla 的创造模式拾取流程继续执行。
 */
@Mixin(Minecraft.class)
public class MixinMinecraftPickBlock {

	@Inject(method = "pickBlockOrEntity", at = @At("HEAD"))
	private void sophisticatedcore$firePickBlockInputEvent(CallbackInfo ci) {
		// pickBlock=true, attack/useItem=false
		InputEvent.InteractionKeyMappingTriggered event = new InputEvent.InteractionKeyMappingTriggered(false, false, true);
		ScNeoForge.EVENT_BUS.post(event);
	}
}
