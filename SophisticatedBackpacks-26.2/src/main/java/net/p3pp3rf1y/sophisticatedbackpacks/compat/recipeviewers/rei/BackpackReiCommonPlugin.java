package net.p3pp3rf1y.sophisticatedbackpacks.compat.recipeviewers.rei;

import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.rei.comparator.ReiSubtypeInterpreter;

import static net.p3pp3rf1y.sophisticatedbackpacks.compat.recipeviewers.common.subtypes.SubtypeInterpreters.getSubtypeInterpreters;

@SuppressWarnings("unused")
public class BackpackReiCommonPlugin implements REICommonPlugin {
	@Override
	public double getPriority() {
		return 0D;
	}

	@Override
	public void registerItemComparators(ItemComparatorRegistry registry) {
		getSubtypeInterpreters().forEach((item, subtypeInterpreter) -> registry.register(ReiSubtypeInterpreter.of(subtypeInterpreter), item));
	}
}
