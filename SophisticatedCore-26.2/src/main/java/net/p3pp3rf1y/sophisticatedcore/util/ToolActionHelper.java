package net.p3pp3rf1y.sophisticatedcore.util;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.Map;
import java.util.Optional;

/**
 * Helper for NeoForge's {@code ItemStack.canPerformAction} and
 * {@code BlockState.getToolModifiedState} extensions.
 *
 * <p>Fabric port that maps ItemAbility constants to vanilla block modification
 * logic. Uses access-widened vanilla fields/methods to access the internal
 * STRIPPABLES, FLATTENABLES maps and WeatheringCopper.getPrevious.</p>
 */
public final class ToolActionHelper {
	private ToolActionHelper() {
	}

	public static boolean canPerformAction(ItemStack stack, ItemAbility action) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		Item item = stack.getItem();
		if (action == ItemAbilities.AXE_STRIP || action == ItemAbilities.AXE_SCRAPE || action == ItemAbilities.AXE_WAX_OFF) {
			return item instanceof AxeItem;
		}
		if (action == ItemAbilities.SHOVEL_FLATTEN) {
			return item instanceof ShovelItem;
		}
		if (action == ItemAbilities.SHEARS_CARVE || action == ItemAbilities.SHEARS_HARVEST) {
			return item instanceof ShearsItem;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static BlockState getToolModifiedState(BlockState state, ItemAbility action) {
		if (state == null) {
			return null;
		}

		if (action == ItemAbilities.AXE_STRIP) {
			Map<Block, Block> strippables = (Map<Block, Block>) AxeItem.STRIPPABLES;
			Block stripped = strippables.get(state.getBlock());
			if (stripped != null) {
				return stripped.defaultBlockState()
						.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
			}
			return null;
		}

		if (action == ItemAbilities.AXE_SCRAPE) {
			Optional<BlockState> previous = WeatheringCopper.getPrevious(state);
			return previous.orElse(null);
		}

		if (action == ItemAbilities.AXE_WAX_OFF) {
			Block unwaxed = HoneycombItem.WAX_OFF_BY_BLOCK.get().get(state.getBlock());
			if (unwaxed != null) {
				return unwaxed.withPropertiesOf(state);
			}
			return null;
		}

		if (action == ItemAbilities.SHOVEL_FLATTEN) {
			Map<Block, BlockState> flattenable = (Map<Block, BlockState>) ShovelItem.FLATTENABLES;
			return flattenable.get(state.getBlock());
		}

		if (action == ItemAbilities.SHEARS_CARVE) {
			if (state.getBlock() == Blocks.PUMPKIN) {
				return Blocks.CARVED_PUMPKIN.defaultBlockState();
			}
			return null;
		}

		if (action == ItemAbilities.SHEARS_HARVEST) {
			if (state.getBlock() instanceof BeehiveBlock) {
				int honeyLevel = state.getValue(BeehiveBlock.HONEY_LEVEL);
				if (honeyLevel >= BeehiveBlock.MAX_HONEY_LEVELS) {
					return state.setValue(BeehiveBlock.HONEY_LEVEL, 0);
				}
			}
			return null;
		}

		return null;
	}
}
