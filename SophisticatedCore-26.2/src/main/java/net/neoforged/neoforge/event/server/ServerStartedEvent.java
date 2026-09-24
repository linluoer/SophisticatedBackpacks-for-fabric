package net.neoforged.neoforge.event.server;

import net.minecraft.server.MinecraftServer;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * 服务器启动完成事件。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。
 */
public class ServerStartedEvent extends Event {
	private final MinecraftServer server;

	public ServerStartedEvent(MinecraftServer server) {
		this.server = server;
	}

	public MinecraftServer getServer() {
		return server;
	}
}
