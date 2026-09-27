package net.neoforged.fml.event.lifecycle;

import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * Event fired during the client setup phase of mod loading.
 * Simplified shim for Fabric port - enqueueWork runs immediately.
 */
public class FMLClientSetupEvent extends Event {
	public void enqueueWork(Runnable work) {
		work.run();
	}
}
