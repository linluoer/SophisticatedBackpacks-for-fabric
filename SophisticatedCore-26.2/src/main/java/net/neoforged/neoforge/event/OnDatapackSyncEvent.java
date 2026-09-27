package net.neoforged.neoforge.event;

import net.minecraft.world.item.crafting.RecipeType;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * OnDatapackSyncEvent shim - fired when datapack/recipe data is synced to clients.
 * <p>
 * On NeoForge this event provides hooks to send custom recipe sync packets.
 * On Fabric, recipe syncing is handled by vanilla's own mechanism, so
 * {@link #sendRecipes} is a no-op.
 */
public class OnDatapackSyncEvent extends Event {
	public OnDatapackSyncEvent() {
	}

	/**
	 * No-op on Fabric. On NeoForge this triggers a custom recipe sync packet
	 * for the given recipe types. Fabric handles recipe sync via vanilla channels.
	 *
	 * @param recipeTypes the recipe types to sync (ignored on Fabric)
	 */
	@SafeVarargs
	public final void sendRecipes(RecipeType<?>... recipeTypes) {
		// no-op: Fabric uses vanilla recipe sync
	}
}
