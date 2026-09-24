package net.p3pp3rf1y.sophisticatedcore.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.util.OpenMenuDataHolder;

/**
 * 携带菜单打开额外数据的 playToClient payload。
 * <p>
 * 服务端在调用 {@code player.openMenu} 之前发送此 payload，
 * 客户端收到后将数据存入 {@link OpenMenuDataHolder}，
 * 随后 {@code ClientboundOpenScreenPacket} 到达时由
 * {@code IContainerFactory.create(int, Inventory)} 消费。
 * <p>
 * 两个包在同一线程上按序处理，保证数据在菜单创建前可用。
 */
public record OpenMenuDataPayload(byte[] data) implements CustomPacketPayload {
	public static final Type<OpenMenuDataPayload> TYPE = new Type<>(SophisticatedCore.getIdentifier("open_menu_data"));

	public static final StreamCodec<RegistryFriendlyByteBuf, OpenMenuDataPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.BYTE_ARRAY, OpenMenuDataPayload::data, OpenMenuDataPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handlePayload(OpenMenuDataPayload payload, IPayloadContext context) {
		Player player = context.player();
		if (player.level().isClientSide()) {
			OpenMenuDataHolder.setPendingData(payload.data());
		}
	}
}
