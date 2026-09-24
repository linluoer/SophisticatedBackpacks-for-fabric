package net.neoforged.neoforge.network.registration;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Registers custom packet payload handlers with the Fabric networking API.
 * Shim for Fabric port - bridges NeoForge's PayloadRegistrar API to Fabric's
 * PayloadTypeRegistry and ServerPlayNetworking/ClientPlayNetworking.
 */
public class PayloadRegistrar {
	private final String modId;
	private final String version;

	public PayloadRegistrar(String modId, String version) {
		this.modId = modId;
		this.version = version;
	}

	public PayloadRegistrar(String modId) {
		this(modId, "1");
	}

	/**
	 * Sets the protocol version and returns this registrar for chaining.
	 */
	public PayloadRegistrar versioned(String version) {
		return new PayloadRegistrar(this.modId, version);
	}

	/**
	 * Marks subsequent registrations as optional (always succeeds in Fabric shim).
	 * NeoForge's optional() returns a registrar that skips registration if the
	 * payload's mod isn't loaded; in Fabric we always register.
	 */
	public PayloadRegistrar optional() {
		return this;
	}

	/**
	 * Registers a server-bound payload (client to server).
	 * The codec is registered with PayloadTypeRegistry.serverboundPlay(),
	 * and the handler is registered with ServerPlayNetworking.
	 */
	public <T extends CustomPacketPayload> PayloadRegistrar playToServer(
			CustomPacketPayload.Type<T> type,
			StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
			BiConsumer<T, IPayloadContext> handler) {

		PayloadTypeRegistry.serverboundPlay().register(type, codec);

		ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
			IPayloadContext ctx = new IPayloadContext() {
				@Override
				public Player player() {
					return context.player();
				}

				@Override
				public void reply(CustomPacketPayload replyPayload) {
					context.responseSender().sendPacket(replyPayload);
				}

				@Override
				public Optional<Object> sender() {
					return Optional.of(context.player());
				}
			};
			handler.accept(payload, ctx);
		});

		return this;
	}

	/**
	 * Registers a client-bound payload (server to client).
	 * The codec is registered with PayloadTypeRegistry.clientboundPlay(),
	 * and the handler is registered with ClientPlayNetworking.
	 */
	public <T extends CustomPacketPayload> PayloadRegistrar playToClient(
			CustomPacketPayload.Type<T> type,
			StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
			BiConsumer<T, IPayloadContext> handler) {

		PayloadTypeRegistry.clientboundPlay().register(type, codec);

		ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
			IPayloadContext ctx = new IPayloadContext() {
				@Override
				public Player player() {
					return context.player();
				}

				@Override
				public void reply(CustomPacketPayload replyPayload) {
					context.responseSender().sendPacket(replyPayload);
				}

				@Override
				public Optional<Object> sender() {
					return Optional.empty();
				}
			};
			handler.accept(payload, ctx);
		});

		return this;
	}

	/**
	 * Registers a bidirectional payload (both client to server and server to client).
	 */
	public <T extends CustomPacketPayload> PayloadRegistrar playBidirectional(
			CustomPacketPayload.Type<T> type,
			StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
			BiConsumer<T, IPayloadContext> serverHandler,
			BiConsumer<T, IPayloadContext> clientHandler) {
		playToServer(type, codec, serverHandler);
		playToClient(type, codec, clientHandler);
		return this;
	}

	public String getModId() {
		return modId;
	}

	public String getVersion() {
		return version;
	}
}
