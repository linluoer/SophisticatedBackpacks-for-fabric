package net.p3pp3rf1y.sophisticatedcore.init;

import net.p3pp3rf1y.sophisticatedcore.compat.CompatInfo;
import net.p3pp3rf1y.sophisticatedcore.compat.CompatModIds;
import net.p3pp3rf1y.sophisticatedcore.compat.CompatRegistry;
import net.p3pp3rf1y.sophisticatedcore.compat.craftingtweaks.CraftingTweaksCompat;
import net.p3pp3rf1y.sophisticatedcore.compat.ftbchunks.FTBChunksCompat;
import net.p3pp3rf1y.sophisticatedcore.compat.inventorysorter.InventorySorterCompat;
import net.p3pp3rf1y.sophisticatedcore.compat.iris.IrisCompat;
import net.p3pp3rf1y.sophisticatedcore.compat.itemborders.ItemBordersCompat;
import net.p3pp3rf1y.sophisticatedcore.compat.mousetweaks.MouseTweaksCompat;
import net.p3pp3rf1y.sophisticatedcore.compat.openpartiesandclaims.OpenPACCompat;
// EMI 兼容代码不存在，已注释
// import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.emi.EmiCompat;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.jei.JeiCompat;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.rei.ReiCompat;
import net.p3pp3rf1y.sophisticatedcore.compat.trashslot.TrashSlotCompat;

public class ModCompat {
	private ModCompat() {
	}

	public static void register() {
		CompatRegistry.registerCompat(new CompatInfo(CompatModIds.JEI), () -> modBus -> new JeiCompat());
		// EMI 兼容代码不存在，已注释
		// CompatRegistry.registerCompat(new CompatInfo(CompatModIds.EMI), () -> modBus -> new EmiCompat());
		CompatRegistry.registerCompat(new CompatInfo(CompatModIds.REI), () -> modBus -> new ReiCompat());
		CompatRegistry.registerCompat(new CompatInfo(CompatModIds.CRAFTING_TWEAKS), () -> modBus -> new CraftingTweaksCompat());
		CompatRegistry.registerCompat(new CompatInfo(CompatModIds.INVENTORY_SORTER), () -> modBus -> new InventorySorterCompat());
		CompatRegistry.registerCompat(new CompatInfo(CompatModIds.ITEM_BORDERS), () -> mobBus -> new ItemBordersCompat());
		// 没有 26.2 版本的兼容模块，已从 sourceSets 排除
		// CompatRegistry.registerCompat(new CompatInfo(CompatModIds.CURIOS), () -> mobBus -> new CuriosCompat());
		// CompatRegistry.registerCompat(new CompatInfo(CompatModIds.CREATE), () -> mobBus -> new CreateCompat());
		CompatRegistry.registerCompat(new CompatInfo(CompatModIds.TRASH_SLOT), () -> mobBus -> new TrashSlotCompat());
		// CompatRegistry.registerCompat(new CompatInfo(CompatModIds.RELIQUARY), () -> mobBus -> new ReliquaryCompat());
		CompatRegistry.registerCompat(new CompatInfo(CompatModIds.MOUSE_TWEAKS), () -> mobBus -> new MouseTweaksCompat());
		CompatRegistry.registerCompat(new CompatInfo(CompatModIds.FTB_CHUNKS), () -> mobBus -> new FTBChunksCompat());
		CompatRegistry.registerCompat(new CompatInfo(CompatModIds.OPEN_PARTIES_AND_CLAIMS_CHUNKS), () -> mobBus -> new OpenPACCompat());
		CompatRegistry.registerCompat(new CompatInfo(CompatModIds.IRIS), () -> mobBus -> new IrisCompat());
		// CompatRegistry.registerCompat(new CompatInfo(CompatModIds.ACCESSORIES), () -> mobBus -> new AccessoriesCompat());
	}
}
