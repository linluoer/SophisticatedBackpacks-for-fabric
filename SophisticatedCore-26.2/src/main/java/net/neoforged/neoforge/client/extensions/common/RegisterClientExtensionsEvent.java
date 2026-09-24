package net.neoforged.neoforge.client.extensions.common;

import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RegisterClientExtensionsEvent shim - event fired to allow mods to register
 * client-side extensions (e.g. fluid rendering). On Fabric this is a
 * self-contained event that stores registrations in an internal map for
 * later lookup.
 */
public class RegisterClientExtensionsEvent extends Event {
	private static final Map<FluidType, IClientFluidTypeExtensions> FLUID_EXTENSIONS = new ConcurrentHashMap<>();

	public RegisterClientExtensionsEvent() {
	}

	/**
	 * Registers client fluid rendering extensions for the given fluid type.
	 *
	 * @param extensions the extensions to register
	 * @param fluidType the fluid type to associate them with
	 */
	public void registerFluidType(IClientFluidTypeExtensions extensions, FluidType fluidType) {
		FLUID_EXTENSIONS.put(fluidType, extensions);
	}

	/**
	 * Looks up the registered client fluid extensions for a fluid type,
	 * returning {@link IClientFluidTypeExtensions#DEFAULT} if none registered.
	 */
	public static IClientFluidTypeExtensions getExtensions(FluidType fluidType) {
		return FLUID_EXTENSIONS.getOrDefault(fluidType, IClientFluidTypeExtensions.DEFAULT);
	}
}
