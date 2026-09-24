package net.neoforged.neoforge.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * EventHooks shim - static utility methods that fire various gameplay events.
 * <p>
 * Mirrors NeoForge's {@code EventHooks} class. On Fabric the corresponding
 * events are either handled by vanilla or by Fabric API, so these methods
 * are mostly no-ops or pass-throughs that return their inputs unchanged.
 */
public final class EventHooks {
	private EventHooks() {
	}

	/**
	 * Fires the item-use-finish event. On Fabric this is a pass-through that
	 * simply returns the supplied result stack.
	 *
	 * @param entity the entity finishing item use
	 * @param stack the item stack being used
	 * @param duration the use duration (unused)
	 * @param result the result item stack from {@code finishUsingItem}
	 * @return the (unmodified) result item stack
	 */
	public static ItemStack onItemUseFinish(LivingEntity entity, ItemStack stack, int duration, ItemStack result) {
		return result;
	}

	/**
	 * Fires the player-smelted event. On Fabric this is a no-op; vanilla
	 * handles recipe unlocking and experience via its own mechanisms.
	 *
	 * @param player the player who smelted the item
	 * @param stack the smelted item stack
	 * @param count the number of items smelted
	 */
	public static void firePlayerSmeltedEvent(Player player, ItemStack stack, int count) {
		// no-op on Fabric
	}
}
