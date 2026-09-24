package net.neoforged.neoforge.client.event;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * Compatibility shim for the NeoForge {@code ScreenEvent} hierarchy.
 * <p>
 * This stub provides the base class plus the most commonly used subclasses:
 * {@link Init}, {@link Render}, {@link KeyPressed}, {@link MouseButtonPressed},
 * {@link MouseButtonReleased} and {@link MouseScrolled}. Each event captures the
 * relevant screen, mouse, key and graphics state needed by consumers.
 */
public abstract class ScreenEvent extends Event {

	private final Screen screen;

	protected ScreenEvent(Screen screen) {
		this.screen = screen;
	}

	public Screen getScreen() {
		return screen;
	}

	/** Screen initialization event (mirrors NeoForge {@code ScreenEvent.Init}). */
	public static class Init extends ScreenEvent {
		protected Init(Screen screen) {
			super(screen);
		}

		/** Fired after a screen has finished its {@code init()} rebuild. */
		public static class Post extends Init {
			public Post(Screen screen) {
				super(screen);
			}
		}
	}

	/** Base class for screen render events. */
	public static abstract class Render extends ScreenEvent {
		private final GuiGraphicsExtractor guiGraphics;
		private final int mouseX;
		private final int mouseY;

		protected Render(Screen screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
			super(screen);
			this.guiGraphics = guiGraphics;
			this.mouseX = mouseX;
			this.mouseY = mouseY;
		}

		public GuiGraphicsExtractor getGuiGraphics() {
			return guiGraphics;
		}

		public int getMouseX() {
			return mouseX;
		}

		public int getMouseY() {
			return mouseY;
		}

		public static class Pre extends Render {
			public Pre(Screen screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
				super(screen, guiGraphics, mouseX, mouseY);
			}
		}

		public static class Post extends Render {
			public Post(Screen screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
				super(screen, guiGraphics, mouseX, mouseY);
			}
		}
	}

	/** Base class for key-press events on a screen. */
	public static abstract class KeyPressed extends ScreenEvent {
		private final int keyCode;
		private final int scanCode;
		private final int modifiers;

		protected KeyPressed(Screen screen, int keyCode, int scanCode, int modifiers) {
			super(screen);
			this.keyCode = keyCode;
			this.scanCode = scanCode;
			this.modifiers = modifiers;
		}

		/** Returns the GLFW/key event code used by {@code InputConstants.getKey(int)}. */
		public int getKeyEvent() {
			return keyCode;
		}

		public int getKey() {
			return keyCode;
		}

		public int getScanCode() {
			return scanCode;
		}

		public int getModifiers() {
			return modifiers;
		}

		public static class Pre extends KeyPressed {
			public Pre(Screen screen, int keyCode, int scanCode, int modifiers) {
				super(screen, keyCode, scanCode, modifiers);
			}
		}

		public static class Post extends KeyPressed {
			public Post(Screen screen, int keyCode, int scanCode, int modifiers) {
				super(screen, keyCode, scanCode, modifiers);
			}
		}
	}

	/** Base class for mouse-button-pressed events on a screen. */
	public static abstract class MouseButtonPressed extends ScreenEvent {
		private final int button;
		private final double mouseX;
		private final double mouseY;

		protected MouseButtonPressed(Screen screen, int button, double mouseX, double mouseY) {
			super(screen);
			this.button = button;
			this.mouseX = mouseX;
			this.mouseY = mouseY;
		}

		public int getButton() {
			return button;
		}

		public double getMouseX() {
			return mouseX;
		}

		public double getMouseY() {
			return mouseY;
		}

		public static class Pre extends MouseButtonPressed {
			public Pre(Screen screen, int button, double mouseX, double mouseY) {
				super(screen, button, mouseX, mouseY);
			}
		}

		public static class Post extends MouseButtonPressed {
			public Post(Screen screen, int button, double mouseX, double mouseY) {
				super(screen, button, mouseX, mouseY);
			}
		}
	}

	/** Base class for mouse-button-released events on a screen. */
	public static abstract class MouseButtonReleased extends ScreenEvent {
		private final int button;
		private final double mouseX;
		private final double mouseY;

		protected MouseButtonReleased(Screen screen, int button, double mouseX, double mouseY) {
			super(screen);
			this.button = button;
			this.mouseX = mouseX;
			this.mouseY = mouseY;
		}

		public int getButton() {
			return button;
		}

		public double getMouseX() {
			return mouseX;
		}

		public double getMouseY() {
			return mouseY;
		}

		public static class Pre extends MouseButtonReleased {
			public Pre(Screen screen, int button, double mouseX, double mouseY) {
				super(screen, button, mouseX, mouseY);
			}
		}

		public static class Post extends MouseButtonReleased {
			public Post(Screen screen, int button, double mouseX, double mouseY) {
				super(screen, button, mouseX, mouseY);
			}
		}
	}

	/** Base class for mouse-scroll events on a screen. */
	public static abstract class MouseScrolled extends ScreenEvent {
		private final double scrollDelta;
		private final double mouseX;
		private final double mouseY;

		protected MouseScrolled(Screen screen, double scrollDelta, double mouseX, double mouseY) {
			super(screen);
			this.scrollDelta = scrollDelta;
			this.mouseX = mouseX;
			this.mouseY = mouseY;
		}

		public double getScrollDelta() {
			return scrollDelta;
		}

		public double getMouseX() {
			return mouseX;
		}

		public double getMouseY() {
			return mouseY;
		}

		public static class Pre extends MouseScrolled {
			public Pre(Screen screen, double scrollDelta, double mouseX, double mouseY) {
				super(screen, scrollDelta, mouseX, mouseY);
			}
		}

		public static class Post extends MouseScrolled {
			public Post(Screen screen, double scrollDelta, double mouseX, double mouseY) {
				super(screen, scrollDelta, mouseX, mouseY);
			}
		}
	}
}
