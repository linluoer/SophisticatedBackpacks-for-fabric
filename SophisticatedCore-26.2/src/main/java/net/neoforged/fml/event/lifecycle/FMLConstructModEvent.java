package net.neoforged.fml.event.lifecycle;

import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * Event fired during the mod construction phase.
 * Simplified shim for Fabric port - enqueueWork runs immediately.
 */
public class FMLConstructModEvent extends Event {
	public void enqueueWork(Runnable work) {
		work.run();
	}
}
