package net.p3pp3rf1y.sophisticatedbackpacks.compat.recipeviewers.common;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.crafting.BackpackUpgradeRecipe;
import net.p3pp3rf1y.sophisticatedbackpacks.crafting.BasicBackpackRecipe;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.ClientRecipeHelper;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.IRecipeViewerDisplayCatalog;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.IRecipeViewerDisplayContext;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.SingleColorDyeRecipeSpec;
import net.p3pp3rf1y.sophisticatedcore.crafting.IWrapperRecipe;

public class BackpackRecipeViewerDisplays {
	private BackpackRecipeViewerDisplays() {
	}

	public static void register(IRecipeViewerDisplayCatalog catalog, IRecipeViewerDisplayContext context) {
		registerDyeRecipes(catalog, context);
		catalog.addCraftingSpecExtensionRecipeClass(BackpackUpgradeRecipe.class);
		BackpackTierUpgradeRecipesMaker.getGroupedShapedCraftingRecipes(context::getSubtypeInterpreter).stream().map(BackpackTierUpgradeDisplayRecipe::toSpec)
				.forEach(catalog::addCraftingSpec);
		BackpackSmithingUpgradeRecipesMaker.getGroupedSmithingRecipes().stream().map(BackpackSmithingUpgradeDisplayRecipe::toSpec)
				.forEach(catalog::addSmithingSpec);
		// 将自定义包装配方转换为普通 ShapedRecipe 后添加到目录，避免 isSpecial()=true 导致 JEI 隐藏；
		// 同时补上原本未注册的 BasicBackpackRecipe（皮革背包合成）。
		ClientRecipeHelper
				.transformAllRecipesOfType(RecipeType.CRAFTING, BasicBackpackRecipe.class,
						(id, recipe) -> new RecipeHolder<CraftingRecipe>(ClientRecipeHelper.recipeKey(id), toShapedRecipe(recipe)))
				.forEach(catalog::addCraftingRecipe);
		ClientRecipeHelper
				.transformAllRecipesOfType(RecipeType.CRAFTING, BackpackUpgradeRecipe.class,
						(id, recipe) -> new RecipeHolder<CraftingRecipe>(ClientRecipeHelper.recipeKey(id), toShapedRecipe(recipe)))
				.forEach(catalog::addCraftingRecipe);
	}

	/**
	 * 将包装类配方（BasicBackpackRecipe / BackpackUpgradeRecipe）转换为普通 ShapedRecipe，
	 * 避免 JEI 因为 isSpecial()=true 而隐藏配方。保留原始 category 和 group 信息。
	 */
	private static ShapedRecipe toShapedRecipe(CraftingRecipe recipe) {
		if (!(recipe instanceof IWrapperRecipe<?> wrapper) || !(wrapper.getCompose() instanceof ShapedRecipe compose)) {
			throw new IllegalArgumentException("Expected IWrapperRecipe<ShapedRecipe>, got " + recipe.getClass());
		}
		return new ShapedRecipe(new Recipe.CommonInfo(true),
				new CraftingRecipe.CraftingBookInfo(compose.category(), compose.group()),
				compose.pattern, ItemStackTemplate.fromNonEmptyStack(ClientRecipeHelper.getResultItem(recipe)));
	}

	public static void registerDyeRecipes(IRecipeViewerDisplayCatalog catalog, IRecipeViewerDisplayContext context) {
		DyeRecipesMaker.getSingleColorRecipeSpecs().stream()
				.map(spec -> new SingleColorDyeRecipeSpec(spec.id(), spec.sourceStacks(), spec.variantPairs(),
						(recipeResult, focusedOutput) -> context.getSubtypeInterpreter(focusedOutput)
								.map(interpreter -> recipeResult.is(focusedOutput.getItem())
										&& interpreter.getComparableData(recipeResult).equals(interpreter.getComparableData(focusedOutput)))
								.orElse(ItemStack.isSameItemSameComponents(recipeResult, focusedOutput))))
				.forEach(catalog::addGroupedCraftingSpec);
		DyeRecipesMaker.getMultipleColorsRecipes().forEach(catalog::addCraftingRecipe);
	}

	public static boolean needsSyntheticSmithingDisplay(ItemStack stack) {
		if (!(stack.getItem() instanceof BackpackItem)) {
			return false;
		}
		BackpackWrapper wrapper = (BackpackWrapper) BackpackWrapper.fromStack(stack);
		return wrapper.getMainColor() != BackpackWrapper.DEFAULT_MAIN_COLOR || wrapper.getAccentColor() != BackpackWrapper.DEFAULT_ACCENT_COLOR;
	}
}
