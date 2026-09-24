package net.neoforged.neoforge.common.extensions;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.IContainerFactory;

/**
 * 菜单类型扩展接口 shim。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。提供基于 {@link IContainerFactory} 创建 {@link MenuType} 的工厂方法，
 * 支持接收额外的缓冲数据。
 * <p>
 * 典型用法：
 * <pre>{@code
 * public static final Supplier<MenuType<MyContainer>> MY_MENU = MENU_TYPES.register("my_menu",
 *         () -> IMenuTypeExtension.create(MyContainer::fromBuffer));
 * }</pre>
 *
 * @param <T> 容器菜单类型
 */
public interface IMenuTypeExtension<T> {

	/**
	 * 创建一个支持额外缓冲数据的 {@link MenuType}。
	 *
	 * @param factory 容器工厂，接收 (windowId, playerInv, extraData)
	 * @param <T>       容器菜单类型，必须为 {@link AbstractContainerMenu} 的子类
	 * @return 新创建的 {@link MenuType}
	 */
	static <T extends AbstractContainerMenu> MenuType<T> create(IContainerFactory<T> factory) {
		return new MenuType<>(factory, FeatureFlags.DEFAULT_FLAGS);
	}

	/**
	 * 创建容器菜单实例，附带额外的缓冲数据。
	 *
	 * @param windowId   窗口 ID
	 * @param playerInv  玩家物品栏
	 * @param extraData  额外的缓冲数据
	 * @return 容器菜单实例
	 */
	T create(int windowId, Inventory playerInv, RegistryFriendlyByteBuf extraData);
}
