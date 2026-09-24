package net.neoforged.neoforge.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.p3pp3rf1y.sophisticatedcore.util.OpenMenuDataHolder;

/**
 * 容器工厂接口 shim - 扩展 vanilla 的 {@link MenuType.MenuSupplier}，
 * 增加一个接收 {@link RegistryFriendlyByteBuf} 额外数据的 {@code create} 重载。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。NeoForge 原版使用此接口创建带额外缓冲数据的容器菜单。
 * 由于 {@link RegistryFriendlyByteBuf} 继承自 {@code FriendlyByteBuf}，
 * 接收 {@code FriendlyByteBuf} 的方法引用（如 {@code SomeContainer::fromBuffer}）
 * 可通过协变赋值给本接口。
 * <p>
 * Fabric 移植中，额外的缓冲数据通过 {@link OpenMenuDataHolder} 在菜单创建前暂存。
 * 默认的 {@code create(int, Inventory)} 方法会消费暂存数据并传递给三参版本。
 *
 * @param <T> 容器菜单类型
 */
@FunctionalInterface
public interface IContainerFactory<T extends AbstractContainerMenu> extends MenuType.MenuSupplier<T> {

	/**
	 * 创建容器菜单实例，附带额外的缓冲数据。
	 *
	 * @param windowId   窗口 ID
	 * @param playerInv  玩家物品栏
	 * @param data       额外的缓冲数据（可能为 null）
	 * @return 容器菜单实例
	 */
	T create(int windowId, Inventory playerInv, RegistryFriendlyByteBuf data);

	/**
	 * 不带额外数据的默认实现。
	 * <p>
	 * 此方法满足 {@link MenuType.MenuSupplier} 接口的要求。
	 * Fabric 移植中，从 {@link OpenMenuDataHolder} 消费暂存的额外数据（如果存在），
	 * 将其包装为 {@link RegistryFriendlyByteBuf} 后传递给三参版本。
	 */
	@Override
	default T create(int containerId, Inventory playerInventory) {
		byte[] data = OpenMenuDataHolder.consumePendingData();
		if (data == null) {
			return create(containerId, playerInventory, null);
		}
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(
				new FriendlyByteBuf(Unpooled.wrappedBuffer(data)),
				playerInventory.player.level().registryAccess()
		);
		return create(containerId, playerInventory, buf);
	}
}
