package net.neoforged.neoforge.client.extensions.common;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * IClientFluidTypeExtensions shim - client-side rendering extensions for a
 * {@link FluidType}. Registered via {@link RegisterClientExtensionsEvent}.
 * <p>
 * Provides texture references used by the fluid model/renderer. Defaults return
 * the vanilla water textures so that unregistered fluids still render.
 */
public interface IClientFluidTypeExtensions {
	Identifier DEFAULT_STILL = Identifier.fromNamespaceAndPath("minecraft", "block/water_still");
	Identifier DEFAULT_FLOWING = Identifier.fromNamespaceAndPath("minecraft", "block/water_flow");

	IClientFluidTypeExtensions DEFAULT = new IClientFluidTypeExtensions() {};

	default Identifier getStillTexture() {
		return DEFAULT_STILL;
	}

	default Identifier getFlowingTexture() {
		return DEFAULT_FLOWING;
	}

	default int getTintColor() {
		return 0xFFFFFFFF;
	}

	default int getTint(FluidType fluidType) {
		return getTintColor();
	}
}
