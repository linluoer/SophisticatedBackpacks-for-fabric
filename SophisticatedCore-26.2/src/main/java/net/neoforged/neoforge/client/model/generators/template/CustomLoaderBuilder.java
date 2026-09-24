package net.neoforged.neoforge.client.model.generators.template;

import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.resources.Identifier;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Builder for custom model loaders in data generation.
 * Fabric shim - delegates to ModelTemplate for basic functionality.
 */
public class CustomLoaderBuilder {
	protected final Identifier loaderId;
	protected final boolean hasRequirements;

	public CustomLoaderBuilder(Identifier loaderId, boolean hasRequirements) {
		this.loaderId = loaderId;
		this.hasRequirements = hasRequirements;
	}

	public CustomLoaderBuilder requiredTextureSlot(TextureSlot slot) {
		return this;
	}

	public ModelTemplate build() {
		return new ModelTemplate(Optional.empty(), Optional.empty());
	}

	protected CustomLoaderBuilder copyInternal() {
		return new CustomLoaderBuilder(loaderId, hasRequirements);
	}
}
