package net.neoforged.neoforge.client.settings;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Compatibility shim for the NeoForge {@code KeyModifier} enumeration.
 * <p>
 * A modifier augments a {@link net.minecraft.client.KeyMapping} so that the
 * binding only fires when the modifier (shift/control/alt) is held at the same
 * time as the primary key. {@link #NONE} matches any key press.
 */
public enum KeyModifier {
	NONE {
		@Override
		public boolean matches(int keyCode) {
			return true;
		}

		@Override
		public boolean isActive() {
			return true;
		}
	},
	SHIFT {
		@Override
		public boolean matches(int keyCode) {
			return keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT;
		}

		@Override
		public boolean isActive() {
			return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)
					|| InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
		}
	},
	CONTROL {
		@Override
		public boolean matches(int keyCode) {
			return keyCode == GLFW.GLFW_KEY_LEFT_CONTROL || keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL;
		}

		@Override
		public boolean isActive() {
			return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL)
					|| InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_RIGHT_CONTROL);
		}
	},
	ALT {
		@Override
		public boolean matches(int keyCode) {
			return keyCode == GLFW.GLFW_KEY_LEFT_ALT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT;
		}

		@Override
		public boolean isActive() {
			return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_ALT)
					|| InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_RIGHT_ALT);
		}
	};

	/**
	 * Determine whether the given key code corresponds to this modifier.
	 *
	 * @param keyCode the GLFW key code
	 * @return {@code true} if the key activates this modifier
	 */
	public abstract boolean matches(int keyCode);

	/**
	 * Determine whether this modifier is currently being held by the player.
	 *
	 * @return {@code true} if the modifier key is currently down
	 */
	public abstract boolean isActive();

	/**
	 * Return the modifier currently being held by the player, preferring
	 * control over shift over alt. Returns {@link #NONE} if no modifier is held.
	 *
	 * @return the active modifier, or {@link #NONE}
	 */
	public static KeyModifier getActiveModifier() {
		if (CONTROL.isActive()) {
			return CONTROL;
		}
		if (SHIFT.isActive()) {
			return SHIFT;
		}
		if (ALT.isActive()) {
			return ALT;
		}
		return NONE;
	}
}
