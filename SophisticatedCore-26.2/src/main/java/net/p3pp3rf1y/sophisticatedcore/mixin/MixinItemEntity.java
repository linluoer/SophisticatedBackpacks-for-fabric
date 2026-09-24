package net.p3pp3rf1y.sophisticatedcore.mixin;

import net.minecraft.util.TriState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedcore.compat.ScNeoForge;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 桥接 {@link ItemEntity#playerTouch(Player)} 到 NeoForge 的
 * {@link ItemEntityPickupEvent.Pre} 事件。
 * <p>
 * SB 的拾取升级监听 {@code CommonEventHandler::onItemPickup}，在升级吸收部分物品后
 * 通过 {@code setCanPickup(TriState.FALSE)} 阻止 vanilla 继续拾取剩余物品。
 * <p>
 * vanilla {@code playerTouch} 在 {@code pickupDelay == 0} 时调用
 * {@code player.getInventory().add(itemStack)}，本 mixin 在其 HEAD 处先触发事件，
 * 若监听器设置 {@code canPickup == FALSE} 则取消 vanilla 拾取流程。
 */
@Mixin(ItemEntity.class)
public class MixinItemEntity {

	@Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
	private void sophisticatedcore$fireItemPickupPre(Player player, CallbackInfo ci) {
		ItemEntity self = (ItemEntity) (Object) this;
		ItemEntityPickupEvent.Pre event = new ItemEntityPickupEvent.Pre(player, self);
		ScNeoForge.EVENT_BUS.post(event);
		if (event.canPickup() == TriState.FALSE) {
			ci.cancel();
		}
	}
}
