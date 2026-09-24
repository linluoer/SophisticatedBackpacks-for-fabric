package net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import net.minecraft.world.item.crafting.RecipeType;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.ClientRecipeHelper;
import net.p3pp3rf1y.sophisticatedcore.crafting.UpgradeNextTierRecipe;
import net.p3pp3rf1y.sophisticatedcore.util.RecipeHelper;

@SuppressWarnings("unused")
public class CoreReiClientPlugin implements REIClientPlugin {
	@Override
	public void registerDisplays(DisplayRegistry registry) {
		RecipeHelper.getRecipesOfType(RecipeType.CRAFTING).stream()
				.filter(r -> r.value() instanceof UpgradeNextTierRecipe)
				.forEach(registry::add);
	}
}
