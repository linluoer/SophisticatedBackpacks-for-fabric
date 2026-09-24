package net.neoforged.neoforge.server;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.util.thread.SidedThreadGroups;
import org.jspecify.annotations.Nullable;

/**
 * ServerLifecycleHooks shim - provides access to the currently running server.
 * <p>
 * On Fabric the current server is obtained via {@code FabricLoader.getInstance().getGameInstance()}
 * which returns a {@link MinecraftServer} on the server side. The server reference is also cached
 * here whenever {@link #setServer(MinecraftServer)} is called so that callers outside the main
 * server thread can still retrieve it.
 */
public final class ServerLifecycleHooks {
	@Nullable
	private static volatile MinecraftServer currentServer;

	private ServerLifecycleHooks() {
	}

	/**
	 * Returns the current server instance, or {@code null} if no server is running.
	 * <p>
	 * This first checks the cached reference, then falls back to querying the Fabric
	 * game instance.
	 */
	@Nullable
	public static MinecraftServer getCurrentServer() {
		MinecraftServer server = currentServer;
		if (server != null) {
			return server;
		}
		Object gameInstance = FabricLoader.getInstance().getGameInstance();
		if (gameInstance instanceof MinecraftServer minecraftServer) {
			return minecraftServer;
		}
		return null;
	}

	/**
	 * Caches the server instance and captures the server thread group.
	 * Called when the server starts.
	 */
	public static void setServer(@Nullable MinecraftServer server) {
		currentServer = server;
		if (server != null) {
			SidedThreadGroups.initServer();
		} else {
			SidedThreadGroups.resetServer();
		}
	}
}
