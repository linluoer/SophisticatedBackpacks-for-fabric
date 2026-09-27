package net.neoforged.neoforge.event.server;

import net.minecraft.server.MinecraftServer;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * 服务器停止事件。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。
 */
public class ServerStoppedEvent extends Event {
	private final MinecraftServer server;

	public ServerStoppedEvent(MinecraftServer server) {
		this.server = server;
	}

	public MinecraftServer getServer() {
		return server;
	}
}
