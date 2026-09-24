package net.neoforged.neoforge.common;

import net.minecraft.world.item.ItemStack;

/**
 * 物品能力 - 代表物品可执行的工具操作（如挖掘、剥皮等）。
 */
public record ItemAbility(String name) {
	public static final ItemAbility AXE_STRIP = new ItemAbility("axe_strip");
	public static final ItemAbility AXE_SCRAPE = new ItemAbility("axe_scrape");
	public static final ItemAbility AXE_WAX_OFF = new ItemAbility("axe_wax_off");
	public static final ItemAbility PICKAXE_DIG = new ItemAbility("pickaxe_dig");
	public static final ItemAbility SHOVEL_DIG = new ItemAbility("shovel_dig");
	public static final ItemAbility AXE_DIG = new ItemAbility("axe_dig");
	public static final ItemAbility HOE_DIG = new ItemAbility("hoe_dig");
	public static final ItemAbility SWORD_DIG = new ItemAbility("sword_dig");
	public static final ItemAbility SHEARS_HARVEST = new ItemAbility("shears_harvest");
	public static final ItemAbility SHEARS_DISARM = new ItemAbility("shears_disarm");
	public static final ItemAbility SHEARS_CARVE = new ItemAbility("shears_carve");
	public static final ItemAbility FISHING_ROD_CAST = new ItemAbility("fishing_rod_cast");
	public static final ItemAbility SHIELD_BLOCK = new ItemAbility("shield_block");

	public static boolean canItemPerformAbility(ItemStack stack, ItemAbility ability) {
		return false;
	}
}
