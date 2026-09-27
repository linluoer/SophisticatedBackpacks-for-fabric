package net.neoforged.neoforge.client.event;

import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * Compatibility shim for the NeoForge {@code ClientTickEvent}.
 * <p>
 * Subclasses mirror the NeoForge Pre/Post split around the client tick loop.
 * No state is carried; the Fabric port fires these directly.
 */
public abstract class ClientTickEvent extends Event {

	/** Fired before the client tick is processed. */
	public static class Pre extends ClientTickEvent {
	}

	/** Fired after the client tick has been processed. */
	public static class Post extends ClientTickEvent {
	}
}
