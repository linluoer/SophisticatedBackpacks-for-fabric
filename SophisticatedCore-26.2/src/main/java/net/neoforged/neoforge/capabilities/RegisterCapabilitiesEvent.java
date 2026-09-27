package net.neoforged.neoforge.capabilities;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

import java.util.function.BiFunction;

/**
 * 能力注册事件 shim - 在 mod 加载阶段触发，用于注册方块/物品/实体的能力提供者。
 * <p>
 * 兼容性 shim 用于 Fabric 移植：所有注册方法均为 no-op，
 * 实际的能力查询通过 Mixin 或直接代码实现。
 * <p>
 * 此事件可被 {@code modBus.addListener(SomeClass::registerCapabilities)} 订阅。
 */
public class RegisterCapabilitiesEvent extends Event {

	/**
	 * 注册方块实体的能力提供者。No-op shim。
	 *
	 * @param capability       方块能力键
	 * @param blockEntityType  方块实体类型
	 * @param provider         能力提供者函数，接收 BlockEntity 与上下文（如 Direction），返回能力实例
	 * @param <T>              能力类型
	 * @param <C>              上下文类型
	 */
	public <T, C> void registerBlockEntity(BlockCapability<T, C> capability, BlockEntityType<?> blockEntityType,
			BiFunction<BlockEntity, C, T> provider) {
		// no-op in shim
	}

	/**
	 * 注册物品的能力提供者。No-op shim。
	 *
	 * @param capability  物品能力键
	 * @param provider    能力提供者函数，接收 ItemStack 与上下文（通常为 Void），返回能力实例
	 * @param items       需要注册能力的物品列表
	 * @param <T>         能力类型
	 * @param <C>         上下文类型
	 */
	public <T, C> void registerItem(ItemCapability<T, C> capability, BiFunction<ItemStack, C, T> provider, Item... items) {
		// no-op in shim
	}

	/**
	 * 注册实体的能力提供者。No-op shim。
	 *
	 * @param capability  实体能力键
	 * @param entityType  实体类型
	 * @param provider    能力提供者函数，接收 Entity 与上下文，返回能力实例
	 * @param <T>         能力类型
	 * @param <C>         上下文类型
	 */
	public <T, C> void registerEntity(EntityCapability<T, C> capability, EntityType<?> entityType,
			BiFunction<Entity, C, T> provider) {
		// no-op in shim
	}
}
