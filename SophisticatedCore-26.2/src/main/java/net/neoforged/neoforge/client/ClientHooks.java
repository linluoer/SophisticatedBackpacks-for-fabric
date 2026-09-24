package net.neoforged.neoforge.client;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.p3pp3rf1y.sophisticatedcore.compat.ScNeoForge;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ClientHooks shim - client-side static utility methods mirroring NeoForge's
 * {@code ClientHooks} / {@code ForgeHooksClient}. Provides tooltip gathering
 * helpers used by the mod's GUI code.
 */
public final class ClientHooks {
	private ClientHooks() {
	}

	/**
	 * Returns the font to use for tooltip rendering. On Fabric there is no
	 * per-stack font override, so the fallback font is returned as-is.
	 */
	public static Font getTooltipFont(ItemStack stack, Font fallbackFont) {
		return fallbackFont;
	}

	/**
	 * Gathers tooltip components for the given stack and text elements, firing
	 * {@link RenderTooltipEvent.GatherComponents} so listeners can modify the list.
	 *
	 * @param stack the item the tooltip is for
	 * @param textElements the text lines
	 * @param tooltipComponent optional non-text tooltip component (e.g. item image)
	 * @param mouseX tooltip x position
	 * @param screenWidth available screen width
	 * @param screenHeight available screen height
	 * @param fallbackFont font to fall back to
	 * @return the laid-out list of client tooltip components
	 */
	public static List<ClientTooltipComponent> gatherTooltipComponents(ItemStack stack, List<? extends FormattedText> textElements,
			Optional<TooltipComponent> tooltipComponent, int mouseX, int screenWidth, int screenHeight, Font fallbackFont) {
		Font font = getTooltipFont(stack, fallbackFont);

		List<Either<FormattedText, TooltipComponent>> elements = textElements.stream()
				.map((FormattedText t) -> Either.<FormattedText, TooltipComponent>left(t))
				.collect(Collectors.toCollection(ArrayList::new));
		tooltipComponent.ifPresent(c -> elements.add(Either.right(c)));

		return processAndLayout(stack, font, elements, mouseX, screenWidth, screenHeight, -1);
	}

	/**
	 * Gathers tooltip components without an explicit non-text component.
	 *
	 * @param stack the item the tooltip is for
	 * @param textElements the text lines
	 * @param mouseX tooltip x position
	 * @param screenWidth available screen width
	 * @param screenHeight available screen height
	 * @param fallbackFont font to fall back to
	 * @return the laid-out list of client tooltip components
	 */
	public static List<ClientTooltipComponent> gatherTooltipComponents(ItemStack stack, List<? extends FormattedText> textElements,
			int mouseX, int screenWidth, int screenHeight, Font fallbackFont) {
		return gatherTooltipComponents(stack, textElements, Optional.empty(), mouseX, screenWidth, screenHeight, fallbackFont);
	}

	private static List<ClientTooltipComponent> processAndLayout(ItemStack stack, Font font,
			List<Either<FormattedText, TooltipComponent>> elements, int mouseX, int screenWidth, int screenHeight, int maxWidth) {
		var event = new RenderTooltipEvent.GatherComponents(stack, screenWidth, screenHeight, elements, maxWidth);
		ScNeoForge.EVENT_BUS.post(event);
		if (event.isCanceled()) {
			return List.of();
		}

		List<Either<FormattedText, TooltipComponent>> tooltipElements = event.getTooltipElements();

		int tooltipTextWidth = tooltipElements.stream()
				.mapToInt(either -> either.map(font::width, component -> 0))
				.max().orElse(0);

		int tooltipX = mouseX + 12;
		if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
			tooltipX = mouseX - 16 - tooltipTextWidth;
			if (tooltipX < 4) {
				if (mouseX > screenWidth / 2) {
					tooltipTextWidth = mouseX - 12 - 8;
				} else {
					tooltipTextWidth = screenWidth - 16 - mouseX;
				}
			}
		}

		if (event.getMaxWidth() > 0 && tooltipTextWidth > event.getMaxWidth()) {
			tooltipTextWidth = event.getMaxWidth();
		}

		final int tooltipTextWidthF = tooltipTextWidth;
		return tooltipElements.stream()
				.flatMap(either -> either.map(
						text -> font.split(text, tooltipTextWidthF).stream().map(ClientTooltipComponent::create),
						component -> Stream.of(ClientTooltipComponent.create(component))))
				.toList();
	}
}
