package net.neoforged.neoforge.fluids;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * SimpleFluidContent shim - stores a FluidStack-like content for use as a DataComponentType value on ItemStacks.
 */
public record SimpleFluidContent(FluidStack fluidStack) {
	public static final SimpleFluidContent EMPTY = new SimpleFluidContent(FluidStack.EMPTY);

	public static final Codec<SimpleFluidContent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			FluidStack.CODEC.fieldOf("fluid").forGetter(SimpleFluidContent::fluidStack)
	).apply(instance, SimpleFluidContent::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, SimpleFluidContent> STREAM_CODEC = StreamCodec.composite(
			FluidStack.STREAM_CODEC, SimpleFluidContent::fluidStack,
			SimpleFluidContent::new
	);

	public static SimpleFluidContent of(FluidStack stack) {
		if (stack == null || stack.isEmpty()) {
			return EMPTY;
		}
		return new SimpleFluidContent(stack.copy());
	}

	public static SimpleFluidContent of(Fluid fluid, int amount) {
		if (fluid == null || fluid == Fluids.EMPTY || amount <= 0) {
			return EMPTY;
		}
		return new SimpleFluidContent(new FluidStack(fluid, amount));
	}

	public static SimpleFluidContent copyOf(FluidStack stack) {
		return of(stack);
	}

	public FluidStack getFluidStack() {
		return fluidStack;
	}

	public FluidStack copy() {
		return fluidStack.copy();
	}

	public boolean isEmpty() {
		return fluidStack.isEmpty();
	}

	public int getAmount() {
		return fluidStack.getAmount();
	}

	public Fluid getFluid() {
		return fluidStack.getFluid();
	}
}
