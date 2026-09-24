package net.neoforged.neoforge.client.event;

import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * Compatibility shim for {@code ClientPlayerNetworkEvent}.
 * <p>
 * Mirrors the NeoForge split into {@link LoggingIn} and {@link LoggingOut}
 * sub-events around the client player joining or leaving a server. The shim
 * carries no state; the Fabric port is expected to fire these directly.
 */
public abstract class ClientPlayerNetworkEvent extends Event {

	/** Fired when the client player is logging into a server. */
	public static class LoggingIn extends ClientPlayerNetworkEvent {
	}

	/** Fired when the client player is logging out of a server. */
	public static class LoggingOut extends ClientPlayerNetworkEvent {
	}
}
