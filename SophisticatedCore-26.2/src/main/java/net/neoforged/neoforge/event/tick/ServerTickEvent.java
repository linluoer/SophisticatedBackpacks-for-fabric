package net.neoforged.neoforge.event.tick;

import net.minecraft.server.MinecraftServer;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * 服务器 tick 事件基类。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。
 */
public abstract class ServerTickEvent extends Event {
	private final MinecraftServer server;
	private final boolean haveTime;

	protected ServerTickEvent(MinecraftServer server, boolean haveTime) {
		this.server = server;
		this.haveTime = haveTime;
	}

	public MinecraftServer getServer() {
		return server;
	}

	public boolean haveTime() {
		return haveTime;
	}

	/**
	 * 服务器 tick 前事件。
	 */
	public static class Pre extends ServerTickEvent {
		public Pre(MinecraftServer server, boolean haveTime) {
			super(server, haveTime);
		}

		public Pre() {
			super(null, true);
		}
	}

	/**
	 * 服务器 tick 后事件。
	 */
	public static class Post extends ServerTickEvent {
		public Post(MinecraftServer server, boolean haveTime) {
			super(server, haveTime);
		}

		public Post() {
			super(null, true);
		}
	}
}
