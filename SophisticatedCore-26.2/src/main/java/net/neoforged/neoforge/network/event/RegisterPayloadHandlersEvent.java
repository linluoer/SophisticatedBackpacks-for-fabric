package net.neoforged.neoforge.network.event;

import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Event fired during payload handler registration.
 * Shim for Fabric port - registrar() creates a new PayloadRegistrar.
 */
public class RegisterPayloadHandlersEvent extends Event {

	public PayloadRegistrar registrar(String modId) {
		return new PayloadRegistrar(modId, "1");
	}
}
