package net.neoforged.neoforge.fluids;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;

import java.util.Optional;

/**
 * FluidStackTemplate shim - an immutable template for a FluidStack (like ItemStackTemplate for ItemStack).
 * Used for serialization of fluid stack render data.
 */
public record FluidStackTemplate(Fluid fluid, int amount) {
	public static final Codec<FluidStackTemplate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			FluidStack.CODEC.fieldOf("fluid").forGetter(FluidStackTemplate::toFluidStack)
	).apply(instance, stack -> fromNonEmptyStack(stack)));

	public static final StreamCodec<RegistryFriendlyByteBuf, FluidStackTemplate> STREAM_CODEC = StreamCodec.composite(
			FluidStack.STREAM_CODEC, FluidStackTemplate::toFluidStack,
			FluidStackTemplate::fromNonEmptyStack
	);

	public static FluidStackTemplate fromNonEmptyStack(FluidStack stack) {
		if (stack.isEmpty()) {
			throw new IllegalArgumentException("Cannot create FluidStackTemplate from empty stack");
		}
		return new FluidStackTemplate(stack.getFluid(), stack.getAmount());
	}

	public FluidStack create() {
		return new FluidStack(fluid, amount);
	}

	private FluidStack toFluidStack() {
		return new FluidStack(fluid, amount);
	}

	public static Optional<FluidStackTemplate> of(FluidStack stack) {
		return stack.isEmpty() ? Optional.empty() : Optional.of(fromNonEmptyStack(stack));
	}
}
