package net.neoforged.neoforge.fluids;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.Objects;
import java.util.Optional;

/**
 * FluidStack shim - represents a stack of fluid (fluid + amount).
 * Mutable amount for compatibility with NeoForge source code.
 */
public class FluidStack {
	public static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY, 0);

	public static final Codec<FluidStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BuiltInRegistries.FLUID.byNameCodec().fieldOf("FluidName").forGetter(FluidStack::getFluid),
			Codec.INT.fieldOf("Amount").forGetter(FluidStack::getAmount)
	).apply(instance, FluidStack::new));

	public static final Codec<FluidStack> OPTIONAL_CODEC = Codec.optionalField("fluid", CODEC, false)
			.xmap(o -> o.orElse(FluidStack.EMPTY), f -> f.isEmpty() ? Optional.empty() : Optional.of(f))
			.codec();

	public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.registry(Registries.FLUID), FluidStack::getFluid,
			ByteBufCodecs.VAR_INT, FluidStack::getAmount,
			FluidStack::new
	);

	private Fluid fluid;
	private int amount;

	public FluidStack(Fluid fluid, int amount) {
		this.fluid = fluid;
		this.amount = amount;
	}

	public Fluid getFluid() {
		return fluid;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public boolean isEmpty() {
		if (this == EMPTY) {
			return true;
		}
		return fluid == Fluids.EMPTY || amount <= 0;
	}

	public FluidStack copy() {
		return new FluidStack(fluid, amount);
	}

	public void shrink(int amount) {
		setAmount(getAmount() - amount);
	}

	public void grow(int amount) {
		setAmount(getAmount() + amount);
	}

	public boolean is(Fluid fluid) {
		return this.fluid == fluid;
	}

	public static boolean isSameFluidSameComponents(FluidStack a, FluidStack b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		return a.fluid == b.fluid;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FluidStack that = (FluidStack) o;
		return amount == that.amount && fluid == that.fluid;
	}

	@Override
	public int hashCode() {
		return Objects.hash(fluid, amount);
	}

	@Override
	public String toString() {
		return "FluidStack{" + fluid + ", " + amount + "}";
	}

	public Component getHoverName() {
		if (fluid == Fluids.EMPTY) {
			return Component.empty();
		}
		// For BaseFlowingFluid (modded fluids), use the FluidType's description
		// because createLegacyBlock() returns AIR for these fluids
		if (fluid instanceof BaseFlowingFluid baseFlowingFluid) {
			return baseFlowingFluid.getFluidType().getDescription();
		}
		return fluid.defaultFluidState().createLegacyBlock().getBlock().getName();
	}

	public DataComponentMap getComponents() {
		return DataComponentMap.EMPTY;
	}
}
