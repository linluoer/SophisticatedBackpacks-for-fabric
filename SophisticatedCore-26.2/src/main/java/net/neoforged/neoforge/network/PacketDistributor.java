package net.neoforged.neoforge.network;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;

/**
 * Distributes packets to various targets on the server side.
 * Shim for Fabric port - delegates to Fabric's ServerPlayNetworking and PlayerLookup.
 */
public class PacketDistributor {
	private PacketDistributor() {
	}

	/**
	 * Sends a payload to the server (from client).
	 */
	public static void sendToServer(CustomPacketPayload payload) {
		net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(payload);
	}

	/**
	 * Sends a payload to a specific player.
	 */
	public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
		ServerPlayNetworking.send(player, payload);
	}

	/**
	 * Sends a payload to all players on the server.
	 */
	public static void sendToAllPlayers(CustomPacketPayload payload) {
		for (ServerPlayer player : PlayerLookup.all(playerServer())) {
			ServerPlayNetworking.send(player, payload);
		}
	}

	/**
	 * Sends a payload to all players near a given position, optionally excluding one player.
	 */
	public static void sendToPlayersNear(ServerLevel level, ServerPlayer exclude, double x, double y, double z, double radius, CustomPacketPayload payload) {
		Vec3 pos = new Vec3(x, y, z);
		Collection<ServerPlayer> players = PlayerLookup.around(level, pos, radius);
		for (ServerPlayer player : players) {
			if (exclude != null && player.getUUID().equals(exclude.getUUID())) {
				continue;
			}
			ServerPlayNetworking.send(player, payload);
		}
	}

	/**
	 * Sends a payload to all players tracking a block entity.
	 */
	public static void sendToTracking(net.minecraft.world.level.block.entity.BlockEntity blockEntity, CustomPacketPayload payload) {
		for (ServerPlayer player : PlayerLookup.tracking(blockEntity)) {
			ServerPlayNetworking.send(player, payload);
		}
	}

	/**
	 * Sends a payload to all players tracking a chunk.
	 */
	public static void sendToTracking(ServerLevel level, net.minecraft.world.level.ChunkPos pos, CustomPacketPayload payload) {
		for (ServerPlayer player : PlayerLookup.tracking(level, pos)) {
			ServerPlayNetworking.send(player, payload);
		}
	}

	/**
	 * Sends a payload to all players tracking an entity.
	 */
	public static void sendToTracking(net.minecraft.world.entity.Entity entity, CustomPacketPayload payload) {
		if (entity.level() instanceof ServerLevel serverLevel) {
			for (ServerPlayer player : PlayerLookup.tracking(entity)) {
				ServerPlayNetworking.send(player, payload);
			}
		}
	}

	/**
	 * Sends a payload to all players tracking an entity and the entity itself if it's a player.
	 */
	public static void sendToPlayersTrackingEntityAndSelf(net.minecraft.world.entity.Entity entity, CustomPacketPayload payload) {
		if (entity.level() instanceof ServerLevel serverLevel) {
			for (ServerPlayer player : PlayerLookup.tracking(entity)) {
				ServerPlayNetworking.send(player, payload);
			}
			if (entity instanceof ServerPlayer serverPlayer) {
				ServerPlayNetworking.send(serverPlayer, payload);
			}
		}
	}

	private static MinecraftServer playerServer() {
		Object instance = FabricLoader.getInstance().getGameInstance();
		if (instance instanceof MinecraftServer server) {
			return server;
		}
		throw new IllegalStateException("PacketDistributor.sendToAllPlayers can only be called on the server side");
	}
}
