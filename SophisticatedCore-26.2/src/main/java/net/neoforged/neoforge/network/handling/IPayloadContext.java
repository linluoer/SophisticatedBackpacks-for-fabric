package net.neoforged.neoforge.network.handling;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

/**
 * Context provided when handling a custom packet payload.
 * Shim for Fabric port - wraps Fabric's networking context.
 */
public interface IPayloadContext {
	/**
	 * Returns the player associated with this payload handling.
	 * On the server, this is the player who sent the payload.
	 * On the client, this is the local player.
	 */
	Player player();

	/**
	 * Sends a reply payload back to the sender.
	 */
	void reply(CustomPacketPayload payload);

	/**
	 * Returns the sender of the payload, if available.
	 * On the server, this is the ServerPlayer who sent it.
	 * On the client, returns empty (server sent it).
	 */
	Optional<Object> sender();

	/**
	 * Returns whether this context is on the client side.
	 */
	default boolean isClientSide() {
		return player() != null && player().level().isClientSide();
	}
}
