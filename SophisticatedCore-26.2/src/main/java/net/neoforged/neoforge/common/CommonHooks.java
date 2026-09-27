package net.neoforged.neoforge.common;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * CommonHooks shim - static utility methods that hook into various vanilla systems.
 * Simplified implementations for Fabric port.
 */
public final class CommonHooks {
	private CommonHooks() {
	}

	@Nullable
	private static Player craftingPlayer;

	public static boolean onItemStackedOn(ItemStack carriedStack, ItemStack slotStack, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess) {
		return false;
	}

	public static void setCraftingPlayer(@Nullable Player player) {
		craftingPlayer = player;
	}

	@Nullable
	public static Player getCraftingPlayer() {
		return craftingPlayer;
	}

	public static ItemStackTemplate getContainerItem(ItemStack stack) {
		return stack.getCraftingRemainder();
	}

	public static boolean onItemDestroy(ItemStack stack, Entity entity) {
		return false;
	}

	public static int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
		return 0;
	}

	public static void onEmptyClick(Player player, InteractionHand hand) {
	}

	public static boolean isCorrectToolForDrops(BlockState state, Player player) {
		return state.requiresCorrectToolForDrops() && player.hasCorrectToolForDrops(state);
	}

	public static InteractionResult onInteractEntityAt(Player player, Entity entity, InteractionHand hand, Vec3 pos) {
		return InteractionResult.PASS;
	}
}
