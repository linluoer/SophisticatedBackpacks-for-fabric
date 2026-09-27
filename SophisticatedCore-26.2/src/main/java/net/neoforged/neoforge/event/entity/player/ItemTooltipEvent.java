package net.neoforged.neoforge.event.entity.player;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

import java.util.List;

/**
 * 物品 Tooltip 事件 shim。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。
 */
public class ItemTooltipEvent extends Event {
	private final ItemStack itemStack;
	private final ItemTooltipFlags flags;
	private final Player player;
	private final List<Component> toolTip;

	public ItemTooltipEvent(ItemStack itemStack, ItemTooltipFlags flags, Player player, List<Component> toolTip) {
		this.itemStack = itemStack;
		this.flags = flags;
		this.player = player;
		this.toolTip = toolTip;
	}

	public ItemStack getItemStack() {
		return itemStack;
	}

	public ItemTooltipFlags getFlags() {
		return flags;
	}

	public Player getEntity() {
		return player;
	}

	public Player getPlayer() {
		return player;
	}

	public List<Component> getToolTip() {
		return toolTip;
	}

	/**
	 * Tooltip 标志位容器 - 封装 {@link TooltipFlag} 及可变标志。
	 */
	public static class ItemTooltipFlags {
		private final TooltipFlag flag;
		private boolean advanced;
		private boolean creative;

		public ItemTooltipFlags(TooltipFlag flag) {
			this.flag = flag;
			this.advanced = false;
			this.creative = false;
		}

		public boolean isAdvanced() {
			return advanced;
		}

		public void setAdvanced(boolean advanced) {
			this.advanced = advanced;
		}

		public boolean isCreative() {
			return creative;
		}

		public void setCreative(boolean creative) {
			this.creative = creative;
		}

		public TooltipFlag getFlag() {
			return flag;
		}
	}
}
