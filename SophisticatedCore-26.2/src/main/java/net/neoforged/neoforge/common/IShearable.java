package net.neoforged.neoforge.common;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

import java.util.Collections;
import java.util.List;

/**
 * 可剪切接口 - 标记方块/实体可被剪刀剪切。
 */
public interface IShearable {
	default List<ItemStack> onSheared(Player player, ItemStack shears, LevelAccessor level, BlockPos pos, int fortune) {
		return Collections.emptyList();
	}

	default boolean isShearable(BlockState state, LevelAccessor level, BlockPos pos) {
		return true;
	}
}
