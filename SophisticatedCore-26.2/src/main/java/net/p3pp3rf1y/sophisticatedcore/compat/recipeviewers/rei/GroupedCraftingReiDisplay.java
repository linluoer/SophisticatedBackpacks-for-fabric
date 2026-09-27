package net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.rei;

import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.GroupedCraftingRecipe;

import java.util.List;
import java.util.Optional;

public class GroupedCraftingReiDisplay extends DefaultCraftingDisplay {
	private final RecipeHolder<GroupedCraftingRecipe> recipeHolder;

	public GroupedCraftingReiDisplay(RecipeHolder<GroupedCraftingRecipe> recipeHolder) {
		super(recipeHolder.value().getInputSlots().stream().map(EntryIngredients::ofItemStacks).toList(),
				List.of(EntryIngredients.ofItemStacks(recipeHolder.value().getResultStacks())),
				Optional.of(recipeHolder.id().identifier()));
		this.recipeHolder = recipeHolder;
	}

	@Override
	public DisplaySerializer<? extends Display> getSerializer() {
		return null;
	}

	@Override
	public boolean isShapeless() {
		return false;
	}

	@Override
	public int getWidth() {
		return recipeHolder.value().getDisplayWidth();
	}

	@Override
	public int getHeight() {
		return recipeHolder.value().getDisplayHeight();
	}

	@Override
	public int getInputWidth(int craftingWidth, int craftingHeight) {
		return getWidth();
	}

	@Override
	public int getInputHeight(int craftingWidth, int craftingHeight) {
		return getHeight();
	}
}
