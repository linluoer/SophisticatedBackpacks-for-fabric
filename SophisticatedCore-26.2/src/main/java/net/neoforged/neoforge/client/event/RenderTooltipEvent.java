package net.neoforged.neoforge.client.event;

import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

import java.util.List;

/**
 * RenderTooltipEvent shim - tooltip rendering events.
 * <p>
 * Provides the {@link GatherComponents} sub-event which is fired before a tooltip
 * is laid out, allowing listeners to add/remove/replace tooltip lines or cancel
 * rendering entirely.
 */
public abstract class RenderTooltipEvent extends Event {
	protected final ItemStack itemStack;

	protected RenderTooltipEvent(ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	public ItemStack getItemStack() {
		return itemStack;
	}

	/**
	 * Fired when tooltip components are being gathered, before they are split
	 * and laid out. Cancelable - canceling prevents the tooltip from rendering.
	 */
	public static class GatherComponents extends RenderTooltipEvent {
		private final int screenWidth;
		private final int screenHeight;
		private final List<Either<FormattedText, TooltipComponent>> tooltipElements;
		private final int maxWidth;

		public GatherComponents(ItemStack itemStack, int screenWidth, int screenHeight,
				List<Either<FormattedText, TooltipComponent>> tooltipElements, int maxWidth) {
			super(itemStack);
			this.screenWidth = screenWidth;
			this.screenHeight = screenHeight;
			this.tooltipElements = tooltipElements;
			this.maxWidth = maxWidth;
		}

		public int getScreenWidth() {
			return screenWidth;
		}

		public int getScreenHeight() {
			return screenHeight;
		}

		public List<Either<FormattedText, TooltipComponent>> getTooltipElements() {
			return tooltipElements;
		}

		public int getMaxWidth() {
			return maxWidth;
		}

		@Override
		public boolean isCancelable() {
			return true;
		}
	}
}
