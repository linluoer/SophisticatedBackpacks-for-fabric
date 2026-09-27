package net.p3pp3rf1y.sophisticatedbackpacks.compat.trinkets;

import eu.pb4.trinkets.api.TrinketInventory;
import eu.pb4.trinkets.api.TrinketsApi;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;
import net.p3pp3rf1y.sophisticatedcore.compat.ICompat;
import org.jspecify.annotations.Nullable;

/**
 * Trinkets 兼容：把 trinkets 的饰品槽接入背包的物品栏查找与渲染链路，
 * 使背包能放进 {@code chest/back} 槽并正常显示、被访问。
 * <p>
 * 本类只在 trinkets 已加载时由 {@link net.p3pp3rf1y.sophisticatedcore.compat.CompatRegistry}
 * 实例化（见 {@link net.p3pp3rf1y.sophisticatedbackpacks.init.ModCompat}），
 * 因此方法体内无需再做加载判断。trinkets 缺失时本类不会被加载，
 * 对 {@code eu.pb4.trinkets} 的引用也不会触发类解析。
 * <p>
 * 渲染层与 render state modifier 由主 mod 自身注册（分别通过
 * {@code LivingEntityRenderLayerRegistrationCallback} 与
 * {@code RegisterRenderStateModifiersEvent}），此处不重复处理。
 */
public class TrinketsCompat implements ICompat {
	private static final String HANDLER_NAME = "trinkets";

	public TrinketsCompat() {
	}

	@Override
	public void setup() {
		PlayerInventoryProvider.get().addPlayerInventoryHandler(HANDLER_NAME,
				player -> TrinketsApi.getAttachment(player).getInventories().keySet(),
				(player, identifier) -> slotCountOf(player, identifier),
				TrinketsCompat::stackOf,
				false, true, false, true,
				TrinketsCompat::isVisible);
	}

	private static int slotCountOf(Player player, String identifier) {
		TrinketInventory inventory = inventoryOf(player, identifier);
		return inventory != null ? inventory.getContainerSize() : 0;
	}

	private static ItemStack stackOf(Player player, String identifier, int slot) {
		TrinketInventory inventory = inventoryOf(player, identifier);
		if (inventory == null || slot >= inventory.getContainerSize()) {
			return ItemStack.EMPTY;
		}
		return inventory.getSlotAccess(slot).get();
	}

	private static boolean isVisible(LivingEntity entity, String identifier, int slot) {
		TrinketInventory inventory = inventoryOf(entity, identifier);
		return inventory != null && slot < inventory.getContainerSize() && inventory.isVisible(slot);
	}

	@Nullable
	private static TrinketInventory inventoryOf(LivingEntity entity, String identifier) {
		return TrinketsApi.getAttachment(entity).getInventories().get(identifier);
	}
}
