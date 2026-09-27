package net.p3pp3rf1y.sophisticatedbackpacks.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.p3pp3rf1y.sophisticatedbackpacks.SophisticatedBackpacks;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BooleanSupplier;

public record BackpackModifierPayload(boolean shortcutBound) implements CustomPacketPayload {
	public static final Type<BackpackModifierPayload> TYPE = new Type<>(SophisticatedBackpacks.getIdentifier("backpack_modifier"));
	public static final StreamCodec<ByteBuf, BackpackModifierPayload> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL,
			BackpackModifierPayload::shortcutBound, BackpackModifierPayload::new);
	private static final Map<Player, Boolean> SHORTCUT_BOUND = new WeakHashMap<>();
	private static BooleanSupplier clientShortcutBound = () -> false;

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handlePayload(BackpackModifierPayload payload, IPayloadContext context) {
		SHORTCUT_BOUND.put(context.player(), payload.shortcutBound());
	}

	public static void setClientShortcutBound(BooleanSupplier supplier) {
		clientShortcutBound = supplier;
	}

	public static boolean isShortcutBound(Player player) {
		return player.level().isClientSide() ? clientShortcutBound.getAsBoolean() : Boolean.TRUE.equals(SHORTCUT_BOUND.get(player));
	}
}
