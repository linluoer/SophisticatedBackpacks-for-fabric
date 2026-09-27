package net.neoforged.fml.event.config;

import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;
import net.neoforged.fml.config.ModConfig;

/**
 * Event fired when a mod config is loaded or reloaded.
 * Simplified shim for Fabric port.
 */
public class ModConfigEvent extends Event {
	private final ModConfig config;

	public ModConfigEvent(ModConfig config) {
		this.config = config;
	}

	public ModConfig getConfig() {
		return config;
	}

	/**
	 * Event fired when a config is first loaded.
	 */
	public static class Loading extends ModConfigEvent {
		public Loading(ModConfig config) {
			super(config);
		}
	}

	/**
	 * Event fired when a config is reloaded.
	 */
	public static class Reloading extends ModConfigEvent {
		public Reloading(ModConfig config) {
			super(config);
		}
	}
}
