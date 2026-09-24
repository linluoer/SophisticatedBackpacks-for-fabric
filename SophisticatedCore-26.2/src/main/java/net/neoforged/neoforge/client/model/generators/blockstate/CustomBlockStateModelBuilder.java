package net.neoforged.neoforge.client.model.generators.blockstate;

import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

/**
 * Base class for custom block state model builders used in data generation.
 * Fabric shim - subclasses implement the abstract methods.
 */
public abstract class CustomBlockStateModelBuilder {
	public abstract CustomBlockStateModelBuilder with(VariantMutator variantMutator);

	public abstract CustomBlockStateModelBuilder with(UnbakedMutator unbakedMutator);

	public abstract CustomUnbakedBlockStateModel toUnbaked();
}
