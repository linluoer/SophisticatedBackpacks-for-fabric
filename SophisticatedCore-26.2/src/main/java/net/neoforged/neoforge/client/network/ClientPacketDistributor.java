package net.neoforged.neoforge.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Distributes packets from the client to the server.
 * Shim for Fabric port - delegates to Fabric's ClientPlayNetworking.
 */
public class ClientPacketDistributor {
	private ClientPacketDistributor() {
	}

	/**
	 * Sends a payload from the client to the server.
	 */
	public static void sendToServer(CustomPacketPayload payload) {
		ClientPlayNetworking.send(payload);
	}
}
