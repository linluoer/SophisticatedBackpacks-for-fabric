package net.neoforged.neoforge.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * ScrollPanel shim - a scrolling container widget.
 * <p>
 * Mirrors the NeoForge {@code ScrollPanel} which provides a clipped, scrollable
 * viewport. Subclasses implement {@link #drawBackground} and {@link #drawPanel}
 * to render content. Mouse wheel and drag adjust {@link #scrollDistance}.
 * <p>
 * This is a functional port for Fabric. The rendering delegates to the abstract
 * {@code drawBackground}/{@code drawPanel} hooks which the NeoForge subclass
 * ({@code InventoryScrollPanel}) already overrides.
 */
public abstract class ScrollPanel extends AbstractWidget {
	protected final Minecraft client;
	/** Current vertical scroll offset in pixels. */
	public double scrollDistance;
	/** Top edge (y) of the panel content area. */
	protected int top;
	/** Left edge (x) of the panel content area. */
	protected int left;
	/** Border thickness around the panel. */
	protected final int border;

	private boolean clickingScrollbar = false;
	private double scrollbarClickY = 0;

	public ScrollPanel(Minecraft client, int width, int height, int top, int left, int border) {
		super(left, top, width, height, Component.empty());
		this.client = client;
		this.width = width;
		this.height = height;
		this.top = top;
		this.left = left;
		this.border = border;
	}

	/** Returns the number of pixels to scroll per click/step. */
	protected abstract int getScrollAmount();

	/** Returns the total height of the content inside the panel. */
	protected abstract int getContentHeight();

	/** Draws the panel background. */
	protected abstract void drawBackground(GuiGraphicsExtractor guiGraphics, float partialTick);

	/** Draws the panel content at the given scroll offset. */
	protected abstract void drawPanel(GuiGraphicsExtractor guiGraphics, int entryRight, int relativeY, int mouseX, int mouseY);

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		drawBackground(guiGraphics, partialTick);
		int contentHeight = getContentHeight();
		int maxScroll = Math.max(0, contentHeight - (height - border * 2));
		if (scrollDistance > maxScroll) {
			scrollDistance = maxScroll;
		}
		if (scrollDistance < 0) {
			scrollDistance = 0;
		}
		// 裁剪内容区域，防止滚动后内容溢出到面板外
		int barWidth = getScrollBarWidth();
		guiGraphics.enableScissor(left + border, top + border, left + width - border - barWidth, top + height - border);
		int relativeY = top + border - (int) scrollDistance;
		drawPanel(guiGraphics, left + width - border, relativeY, mouseX, mouseY);
		guiGraphics.disableScissor();
		drawScrollBar(guiGraphics, mouseX, mouseY);
	}

	/** 滚动条宽度（面板右侧预留区域） */
	protected int getScrollBarWidth() {
		return 6;
	}

	/**
	 * 绘制滚动条轨道与滑块。
	 * <p>
	 * 原 NeoForge ScrollPanel 基于纹理渲染，这里以纯色矩形还原：
	 * 轨道深色背景 + 浅色滑块（悬停高亮）。
	 */
	protected void drawScrollBar(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		int contentHeight = getContentHeight();
		int viewHeight = height - border * 2;
		if (contentHeight <= viewHeight) {
			return; // 内容未溢出，无需滚动条
		}
		int barHeight = getBarHeight();
		int trackTop = top + border;
		int trackBottom = top + height - border;
		int maxScroll = Math.max(1, contentHeight - viewHeight);
		int barTop = trackTop + (int) ((scrollDistance / (double) maxScroll) * (viewHeight - barHeight));
		int barLeft = left + width - getScrollBarWidth();
		int barRight = barLeft + getScrollBarWidth();
		// 轨道
		guiGraphics.fill(barLeft, trackTop, barRight, trackBottom, 0xFF_000000);
		guiGraphics.fill(barLeft + 1, trackTop, barRight - 1, trackBottom, 0xFF_2A2A2A);
		// 滑块（悬停高亮）
		boolean hovered = mouseX >= barLeft && mouseX <= barRight && mouseY >= barTop && mouseY <= barTop + barHeight;
		int thumbColor = hovered ? 0xFF_C8C8C8 : 0xFF_8B8B8B;
		guiGraphics.fill(barLeft, barTop, barRight, barTop + barHeight, thumbColor);
	}

	protected int getBarHeight() {
		int contentHeight = getContentHeight();
		int barHeight = (height - border * 2) * (height - border * 2) / Math.max(1, contentHeight);
		if (barHeight < 32) {
			barHeight = 32;
		}
		if (barHeight > height - border * 2) {
			barHeight = height - border * 2;
		}
		return barHeight;
	}

	@Override
	public NarrationPriority narrationPriority() {
		return NarrationPriority.NONE;
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		// no narration
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= left && mouseX <= left + width && mouseY >= top && mouseY <= top + height;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (isMouseOver(mouseX, mouseY)) {
			// 26.2 的 scrollY 单位为行（±1），直接乘以每行滚动量
			scrollDistance += -scrollY * getScrollAmount();
			applyScrollLimits();
			return true;
		}
		return false;
	}

	private void applyScrollLimits() {
		int maxScroll = Math.max(0, getContentHeight() - (height - border * 2));
		if (scrollDistance > maxScroll) {
			scrollDistance = maxScroll;
		}
		if (scrollDistance < 0) {
			scrollDistance = 0;
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.buttonInfo().button() != 0 || !isMouseOver(event.x(), event.y())) {
			return false;
		}
		clickingScrollbar = true;
		scrollbarClickY = event.y();
		return true;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (clickingScrollbar) {
			int contentHeight = getContentHeight();
			int barHeight = getBarHeight();
			int barTop = top + border;
			int barBottom = top + height - border - barHeight;
			if (barBottom > barTop) {
				double ratio = (event.y() - barTop - barHeight / 2.0) / (barBottom - barTop);
				scrollDistance = ratio * Math.max(1, contentHeight - (height - border * 2));
				applyScrollLimits();
			}
			return true;
		}
		return false;
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		// handled in mouseClicked override
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		clickingScrollbar = false;
		return super.mouseReleased(event);
	}
}
