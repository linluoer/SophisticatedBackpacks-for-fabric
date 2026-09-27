package net.neoforged.neoforge.common;

/**
 * 物品能力常量集合 - 提供预定义的 {@link ItemAbility} 实例。
 * <p>
 * 兼容性 shim：与 {@link ItemAbility} 配合使用，
 * 供 Sophisticated 系列模组通过 {@code import static net.neoforged.neoforge.common.ItemAbilities.*}
 * 引入这些常量。
 */
public final class ItemAbilities {
	private ItemAbilities() {
	}

	// 默认挖掘动作
	public static final ItemAbility DEFAULT_AXE_DIG = new ItemAbility("default_axe_dig");
	public static final ItemAbility DEFAULT_PICKAXE_DIG = new ItemAbility("default_pickaxe_dig");
	public static final ItemAbility DEFAULT_SHOVEL_DIG = new ItemAbility("default_shovel_dig");
	public static final ItemAbility DEFAULT_HOE_DIG = new ItemAbility("default_hoe_dig");
	public static final ItemAbility DEFAULT_SWORD_DIG = new ItemAbility("default_sword_dig");

	// 工具类型对应的挖掘动作
	public static final ItemAbility AXE_DIG = ItemAbility.AXE_DIG;
	public static final ItemAbility PICKAXE_DIG = ItemAbility.PICKAXE_DIG;
	public static final ItemAbility SHOVEL_DIG = ItemAbility.SHOVEL_DIG;
	public static final ItemAbility HOE_DIG = ItemAbility.HOE_DIG;
	public static final ItemAbility SWORD_DIG = ItemAbility.SWORD_DIG;

	// 斧头特有动作
	public static final ItemAbility AXE_STRIP = ItemAbility.AXE_STRIP;
	public static final ItemAbility AXE_SCRAPE = ItemAbility.AXE_SCRAPE;
	public static final ItemAbility AXE_WAX_OFF = ItemAbility.AXE_WAX_OFF;

	// 铲子特有动作（原 ItemAbility 中缺失，在此补充）
	public static final ItemAbility SHOVEL_FLATTEN = new ItemAbility("shovel_flatten");

	// 剪刀特有动作
	public static final ItemAbility SHEARS_HARVEST = ItemAbility.SHEARS_HARVEST;
	public static final ItemAbility SHEARS_DISARM = ItemAbility.SHEARS_DISARM;
	public static final ItemAbility SHEARS_CARVE = ItemAbility.SHEARS_CARVE;

	// 其他动作
	public static final ItemAbility FISHING_ROD_CAST = ItemAbility.FISHING_ROD_CAST;
	public static final ItemAbility SHIELD_BLOCK = ItemAbility.SHIELD_BLOCK;
}
