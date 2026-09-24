package net.neoforged.neoforge.client.event;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * Compatibility shim for {@code RegisterKeyMappingsEvent}.
 * <p>
 * Delegates key mapping registration to Fabric's {@link KeyMappingHelper} so
 * NeoForge-style registration calls actually appear in the controls screen.
 */
public class RegisterKeyMappingsEvent extends Event {

	/**
	 * Register a key mapping with Fabric's {@link KeyMappingHelper}.
	 *
	 * @param key the key mapping to register
	 */
	public void register(KeyMapping key) {
		KeyMappingHelper.registerKeyMapping(key);
	}

	/**
	 * Register a key mapping category so it appears in the correct position
	 * in the controls screen sort order.
	 *
	 * @param category the category to register
	 */
	public void registerCategory(KeyMapping.Category category) {
		try {
			KeyMapping.Category.register(category.id());
		} catch (IllegalArgumentException alreadyRegistered) {
			// category already registered, ignore
		}
	}
}
