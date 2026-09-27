package net.p3pp3rf1y.sophisticatedcore.mixin;

import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Mixin into {@link FluidStateModelSet} to merge custom fluid model registrations
 * (registered via {@link RegisterFluidModelsEvent}) into the vanilla fluid model map.
 * <p>
 * Without this, custom fluids like XP fluid have no model and render with the
 * missing texture (pink/black checkerboard) in the tank upgrade GUI.
 */
@Mixin(FluidStateModelSet.class)
public class MixinFluidStateModelSet {

	@Inject(method = "bake", at = @At("RETURN"), cancellable = true)
	private static void sophisticatedcore$mergeCustomFluidModels(MaterialBaker materials,
			CallbackInfoReturnable<Map<Fluid, FluidModel>> cir) {
		Map<Fluid, FluidModel.Unbaked> customRegistrations = RegisterFluidModelsEvent.getRegistrations();
		if (customRegistrations.isEmpty()) {
			return;
		}
		Map<Fluid, FluidModel> result = new IdentityHashMap<>(cir.getReturnValue());
		customRegistrations.forEach((fluid, unbaked) -> {
			FluidModel baked = unbaked.bake(materials, () -> "SophisticatedCore fluid " + fluid);
			result.put(fluid, baked);
		});
		cir.setReturnValue(result);
	}
}
