package net.neoforged.neoforge.client.event;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * ContainerScreenEvent shim - events fired around container screen rendering.
 * <p>
 * Provides the {@link Render} sub-event with {@link Render.Pre} and
 * {@link Render.Foreground} hooks. The mod posts {@code Render.Foreground}
 * directly to the event bus to allow other code to draw on top of container
 * foregrounds.
 */
public abstract class ContainerScreenEvent extends Event {
	private final AbstractContainerScreen<?> containerScreen;
	private final GuiGraphicsExtractor guiGraphics;
	private final int mouseX;
	private final int mouseY;

	protected ContainerScreenEvent(AbstractContainerScreen<?> containerScreen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		this.containerScreen = containerScreen;
		this.guiGraphics = guiGraphics;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
	}

	public AbstractContainerScreen<?> getContainerScreen() {
		return containerScreen;
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

	/** Base class for container screen render events. */
	public static abstract class Render extends ContainerScreenEvent {
		protected Render(AbstractContainerScreen<?> containerScreen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
			super(containerScreen, guiGraphics, mouseX, mouseY);
		}

		/** Fired before the container screen foreground is rendered. */
		public static class Pre extends Render {
			public Pre(AbstractContainerScreen<?> containerScreen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
				super(containerScreen, guiGraphics, mouseX, mouseY);
			}
		}

		/** Fired after the container screen foreground is rendered. */
		public static class Foreground extends Render {
			public Foreground(AbstractContainerScreen<?> containerScreen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
				super(containerScreen, guiGraphics, mouseX, mouseY);
			}
		}
	}
}
