package net.neoforged.neoforge.transfer.fluid;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidTypeLookup;
import net.neoforged.neoforge.transfer.Resource;

/**
 * 流体资源 - 代表一种流体（不含数量）。
 */
public final class FluidResource implements Resource {
	public static final FluidResource EMPTY = new FluidResource(Fluids.EMPTY);

	private final Fluid fluid;

	private FluidResource(Fluid fluid) {
		this.fluid = fluid;
	}

	public static FluidResource of(Fluid fluid) {
		if (fluid == null || fluid == Fluids.EMPTY) {
			return EMPTY;
		}
		return new FluidResource(fluid);
	}

	public static FluidResource of(FluidStack stack) {
		if (stack == null || stack.isEmpty()) {
			return EMPTY;
		}
		return of(stack.getFluid());
	}

	public Fluid getFluid() {
		return fluid;
	}

	public FluidType getFluidType() {
		return FluidTypeLookup.getFluidType(fluid);
	}

	public FluidStack toStack(int amount) {
		if (isEmpty()) {
			return FluidStack.EMPTY;
		}
		return new FluidStack(fluid, amount);
	}

	public boolean matches(FluidStack stack) {
		if (stack == null || stack.isEmpty()) {
			return isEmpty();
		}
		return this.fluid == stack.getFluid();
	}

	public boolean is(TagKey<Fluid> tag) {
		return fluid != null && fluid.is(tag);
	}

	@Override
	public boolean isEmpty() {
		return fluid == Fluids.EMPTY;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FluidResource that = (FluidResource) o;
		return fluid == that.fluid;
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(fluid);
	}

	@Override
	public String toString() {
		return "FluidResource[" + fluid + "]";
	}
}
