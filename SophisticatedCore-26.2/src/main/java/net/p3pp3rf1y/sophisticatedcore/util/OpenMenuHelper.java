package net.p3pp3rf1y.sophisticatedcore.util;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.network.OpenMenuDataPayload;

import java.util.function.Consumer;

/**
 * Helper for NeoForge's dual-parameter {@code Player.openMenu(MenuProvider, Consumer<RegistryFriendlyByteBuf>)}.
 *
 * <p>In NeoForge, the second parameter writes extra data that is sent to the client
 * and used by {@code IContainerFactory.create} to reconstruct the menu on the client side.</p>
 *
 * <p>Fabric port: 在调用 vanilla {@code openMenu} 之前，先将额外数据通过
 * {@link OpenMenuDataPayload} 发送到客户端。客户端 handler 将数据存入
 * {@link OpenMenuDataHolder}，随后 {@code ClientboundOpenScreenPacket} 到达时
 * 由 {@code IContainerFactory.create(int, Inventory)} 消费。</p>
 */
public final class OpenMenuHelper {
	private OpenMenuHelper() {
	}

	public static void openMenu(Player player, MenuProvider provider, Consumer<RegistryFriendlyByteBuf> dataWriter) {
		if (player instanceof ServerPlayer serverPlayer) {
			RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(
					new FriendlyByteBuf(Unpooled.buffer()), serverPlayer.registryAccess());
			dataWriter.accept(buf);
			byte[] data = new byte[buf.readableBytes()];
			buf.readBytes(data);
			buf.release();

			// 先发送数据 payload，再调用 openMenu（发送 ClientboundOpenScreenPacket）
			// 两个包在客户端主线程按序处理，保证数据在菜单创建前可用
			PacketDistributor.sendToPlayer(serverPlayer, new OpenMenuDataPayload(data));
			serverPlayer.openMenu(provider);
		} else {
			player.openMenu(provider);
		}
	}
}
