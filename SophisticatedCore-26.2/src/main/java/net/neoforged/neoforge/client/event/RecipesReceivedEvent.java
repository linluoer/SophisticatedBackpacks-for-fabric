package net.neoforged.neoforge.client.event;

import net.minecraft.world.item.crafting.RecipeMap;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * RecipesReceivedEvent shim - fired on the client when the server sends
 * the recipe collection during sync.
 * <p>
 * The mod uses this to populate the client-side recipe cache via
 * {@link #getRecipeMap()}.
 */
public class RecipesReceivedEvent extends Event {
	private final RecipeMap recipeMap;

	public RecipesReceivedEvent(RecipeMap recipeMap) {
		this.recipeMap = recipeMap;
	}

	public RecipeMap getRecipeMap() {
		return recipeMap;
	}
}
