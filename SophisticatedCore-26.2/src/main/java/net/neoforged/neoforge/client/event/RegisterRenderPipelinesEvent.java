package net.neoforged.neoforge.client.event;

import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * Compatibility shim for {@code RegisterRenderPipelinesEvent}.
 * <p>
 * Fired to allow mods to register custom render pipelines. The shim is a
 * stub that exposes the registration method used by listeners; the Fabric
 * port is expected to bridge to its own render pipeline registration.
 */
public class RegisterRenderPipelinesEvent extends Event {

	/**
	 * Register a render pipeline. In the port layer this delegates to the
	 * appropriate Fabric rendering hook.
	 *
	 * @param pipeline the render pipeline to register
	 */
	public void registerPipeline(Object pipeline) {
		// no-op shim
	}
}
