package net.neoforged.neoforge.fluids;

import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * FluidUtil shim - static utility methods for fluid operations.
 * Simplified implementation for Fabric port.
 */
public final class FluidUtil {
	private FluidUtil() {
	}

	public static Optional<FluidStack> getFluidContained(ItemStack stack) {
		return Optional.empty();
	}

	public static boolean hasFluidHandler(ItemStack stack) {
		return false;
	}
}
