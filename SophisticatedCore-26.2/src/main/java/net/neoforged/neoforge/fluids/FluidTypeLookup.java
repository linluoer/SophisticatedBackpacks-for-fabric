package net.neoforged.neoforge.fluids;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FluidType lookup helper - maps Fluid to FluidType.
 * Fluid types are registered through NeoForgeRegistries.FLUID_TYPES or directly here.
 */
public final class FluidTypeLookup {
	private static final FluidType DEFAULT_TYPE = new FluidType(FluidType.Properties.create());
	private static final Map<Fluid, FluidType> FLUID_TYPES = new ConcurrentHashMap<>();

	private FluidTypeLookup() {
	}

	public static FluidType getFluidType(Fluid fluid) {
		if (fluid == null || fluid == Fluids.EMPTY) {
			return DEFAULT_TYPE;
		}
		return FLUID_TYPES.getOrDefault(fluid, DEFAULT_TYPE);
	}

	public static void register(Fluid fluid, FluidType type) {
		FLUID_TYPES.put(fluid, type);
	}
}
