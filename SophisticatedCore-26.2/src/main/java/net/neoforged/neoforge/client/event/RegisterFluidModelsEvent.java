package net.neoforged.neoforge.client.event;

import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.world.level.material.Fluid;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Compatibility shim for {@code RegisterFluidModelsEvent}.
 * <p>
 * Fired to allow mods to register custom fluid models. The shim stores
 * registrations in a static map so that a mixin on
 * {@link net.minecraft.client.renderer.block.FluidStateModelSet} can merge
 * them into the vanilla fluid model map during baking.
 */
public class RegisterFluidModelsEvent extends Event {

	private static final Map<Fluid, FluidModel.Unbaked> REGISTRY = new ConcurrentHashMap<>();

	/**
	 * Register a fluid model with its still and flowing fluids.
	 *
	 * @param model       the unbaked fluid model to register
	 * @param stillFluid  the still fluid
	 * @param flowingFluid the flowing fluid
	 */
	public void register(FluidModel.Unbaked model, Fluid stillFluid, Fluid flowingFluid) {
		REGISTRY.put(stillFluid, model);
		REGISTRY.put(flowingFluid, model);
	}

	/**
	 * Returns the registered custom fluid model registrations.
	 * Called by {@code MixinFluidStateModelSet} during model baking.
	 */
	public static Map<Fluid, FluidModel.Unbaked> getRegistrations() {
		return REGISTRY;
	}
}
