package net.neoforged.fml.event.lifecycle;

import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * Shim for neoforge InterModEnqueueEvent.
 * On Fabric, enqueueWork runs immediately.
 */
public class InterModEnqueueEvent extends Event {
	public void enqueueWork(Runnable work) {
		work.run();
	}
}
