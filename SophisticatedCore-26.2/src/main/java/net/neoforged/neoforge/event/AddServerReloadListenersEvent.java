package net.neoforged.neoforge.event;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * 添加服务器重载监听器事件 - 在服务器资源管理器初始化时触发。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。
 */
public class AddServerReloadListenersEvent extends Event {
	private final MinecraftServer server;
	private final List<ListenerEntry> entries = new ArrayList<>();

	public AddServerReloadListenersEvent(MinecraftServer server) {
		this.server = server;
	}

	public MinecraftServer getServer() {
		return server;
	}

	/**
	 * 添加一个重载监听器。
	 *
	 * @param name 监听器的标识符
	 * @param listener 重载监听器
	 */
	public void addListener(Identifier name, PreparableReloadListener listener) {
		entries.add(new ListenerEntry(name, listener));
	}

	public List<ListenerEntry> getEntries() {
		return entries;
	}

	public static class ListenerEntry {
		private final Identifier name;
		private final PreparableReloadListener listener;

		public ListenerEntry(Identifier name, PreparableReloadListener listener) {
			this.name = name;
			this.listener = listener;
		}

		public Identifier name() {
			return name;
		}

		public PreparableReloadListener listener() {
			return listener;
		}
	}
}
