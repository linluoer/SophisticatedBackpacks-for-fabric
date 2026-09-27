package net.neoforged.neoforge.client.event;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * 注册菜单界面事件 shim。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。{@link #register} 桥接到 vanilla 26.2 的 {@link MenuScreens#register}。
 */
public class RegisterMenuScreensEvent extends Event {

	@FunctionalInterface
	public interface MenuConstructor<M extends AbstractContainerMenu> {
		Screen create(M menu, Inventory inventory, Component title);
	}

	/**
	 * 注册菜单界面，桥接到 vanilla {@link MenuScreens#register}。
	 * <p>
	 * 使用 raw type 转换绕过泛型约束，因为 {@link MenuConstructor} 返回 {@link Screen}，
	 * 而 {@link MenuScreens.ScreenConstructor} 要求 {@code U extends Screen & MenuAccess<M>}。
	 * 实际实现中，屏幕类继承自 {@code AbstractContainerScreen}，已实现 {@code MenuAccess}。
	 *
	 * @param type    菜单类型
	 * @param factory 屏幕构造器
	 * @param <M>     菜单类型参数
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <M extends AbstractContainerMenu> void register(MenuType<M> type, MenuConstructor<M> factory) {
		MenuScreens.register(type, (MenuScreens.ScreenConstructor) (menu, inv, title) ->
				factory.create((M) menu, inv, title));
	}
}
