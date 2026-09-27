package net.neoforged.neoforge.client.settings;

import net.minecraft.client.Minecraft;

/**
 * Compatibility shim for the standard NeoForge {@code KeyConflictContext}
 * enumeration.
 * <p>
 * The contexts mirror the vanilla/NeoForge split between keys that always
 * apply, keys that only apply when a GUI screen is open, and keys that only
 * apply while in-game.
 */
public enum KeyConflictContext implements IKeyConflictContext {

	/** Active in every context (both GUI and in-game). */
	UNIVERSAL {
		@Override
		public boolean isActive() {
			return true;
		}

		@Override
		public boolean conflicts(IKeyConflictContext other) {
			return true;
		}
	},

	/** Active only while a GUI screen is open. */
	GUI {
		@Override
		public boolean isActive() {
			return Minecraft.getInstance() != null && Minecraft.getInstance().gui.screen() != null;
		}

		@Override
		public boolean conflicts(IKeyConflictContext other) {
			return this == other || other == UNIVERSAL;
		}
	},

	/** Active only while no GUI screen is open (in-game). */
	IN_GAME {
		@Override
		public boolean isActive() {
			return Minecraft.getInstance() != null && Minecraft.getInstance().gui.screen() == null;
		}

		@Override
		public boolean conflicts(IKeyConflictContext other) {
			return this == other || other == UNIVERSAL;
		}
	}
}
