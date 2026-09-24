package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * Compatibility shim for {@code SubmitCustomGeometryEvent}.
 * <p>
 * Fired to allow mods to submit custom geometry for rendering. The shim is a
 * stub that exposes the accessors used by listeners; the Fabric port is
 * expected to bridge to its own rendering pipeline.
 */
public class SubmitCustomGeometryEvent extends Event {

	/**
	 * Return the submit node collector used to gather geometry submissions.
	 *
	 * @return the submit node collector (stub)
	 */
	public Object getSubmitNodeCollector() {
		return null;
	}

	/**
	 * Return the pose stack used for rendering.
	 *
	 * @return the pose stack (stub)
	 */
	public PoseStack getPoseStack() {
		return null;
	}

	/**
	 * Return the level render state.
	 *
	 * @return the level render state (stub)
	 */
	public Object getLevelRenderState() {
		return null;
	}
}
