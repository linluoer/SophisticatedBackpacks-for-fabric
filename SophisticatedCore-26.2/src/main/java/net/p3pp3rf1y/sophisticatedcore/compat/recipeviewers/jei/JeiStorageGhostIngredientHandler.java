package net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.common.gui.IFilterSlot;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.SetGhostSlotPayload;

import java.util.ArrayList;
import java.util.List;

public class JeiStorageGhostIngredientHandler<S extends StorageScreenBase<?>> implements IGhostIngredientHandler<S> {
	@Override
	public <I> List<Target<I>> getTargetsTyped(S gui, ITypedIngredient<I> ingredient, boolean doStart) {
		List<Target<I>> targets = new ArrayList<>();
		if (ingredient.getType() == VanillaTypes.ITEM_STACK) {
			StorageContainerMenuBase<?> container = gui.getMenu();
			ingredient.getItemStack().ifPresent(ghostStack -> {
				container.getOpenContainer().ifPresent(c -> c.getSlots().forEach(s -> {
					if (s instanceof IFilterSlot && s.mayPlace(ghostStack)) {
						targets.add(new Target<>() {
							@Override
							public Rect2i getArea() {
								return new Rect2i(gui.leftPos + s.x, gui.topPos + s.y, 17, 17);
							}

							@Override
							public void accept(I i) {
								ClientPacketDistributor.sendToServer(new SetGhostSlotPayload(ghostStack, s.index));
							}
						});
					}
				}));
			});
		}
		// 流体 ghost ingredient 暂时不支持（需要 Fabric Fluid API 适配）
		return targets;
	}

	@Override
	public void onComplete() {
		// noop
	}
}
