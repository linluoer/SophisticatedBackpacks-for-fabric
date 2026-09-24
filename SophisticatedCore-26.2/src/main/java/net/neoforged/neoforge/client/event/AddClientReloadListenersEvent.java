package net.neoforged.neoforge.client.event;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Compatibility shim for {@code AddClientReloadListenersEvent}.
 * <p>
 * Fired to allow mods to register client-side resource reload listeners. The
 * shim collects listeners into a static list; the Fabric entrypoint is
 * responsible for draining the list and registering them via Fabric's
 * {@code ResourceManagerHelper}.
 */
public class AddClientReloadListenersEvent extends Event {

	private static final List<Map.Entry<Identifier, PreparableReloadListener>> PENDING_LISTENERS = new CopyOnWriteArrayList<>();

	/**
	 * Add a client-side resource reload listener under the given identifier.
	 *
	 * @param name     the identifier of the reload listener
	 * @param listener the reload listener to add
	 */
	public void addListener(Identifier name, PreparableReloadListener listener) {
		PENDING_LISTENERS.add(Map.entry(name, listener));
	}

	/**
	 * Drain and return all pending reload listeners registered via this event.
	 * Called by the Fabric entrypoint to bridge to {@code ResourceManagerHelper}.
	 *
	 * @return a new list containing all pending listeners
	 */
	public static List<Map.Entry<Identifier, PreparableReloadListener>> drainPendingListeners() {
		List<Map.Entry<Identifier, PreparableReloadListener>> result = new ArrayList<>(PENDING_LISTENERS);
		PENDING_LISTENERS.clear();
		return result;
	}
}
