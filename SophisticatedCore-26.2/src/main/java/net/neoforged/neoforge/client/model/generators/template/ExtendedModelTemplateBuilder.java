package net.neoforged.neoforge.client.model.generators.template;

import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Extended model template builder for data generation.
 * Fabric shim - provides a simplified API compatible with NeoForge's data generation.
 * Transform and customLoader settings are recorded but not fully applied in this shim.
 */
public class ExtendedModelTemplateBuilder {
	private Optional<Identifier> parent = Optional.empty();

	private ExtendedModelTemplateBuilder() {
	}

	public static ExtendedModelTemplateBuilder builder() {
		return new ExtendedModelTemplateBuilder();
	}

	public ExtendedModelTemplateBuilder parent(Identifier parent) {
		this.parent = Optional.of(parent);
		return this;
	}

	public ExtendedModelTemplateBuilder transform(ItemDisplayContext context, Consumer<TransformBuilder> transform) {
		transform.accept(new TransformBuilder());
		return this;
	}

	public <T extends CustomLoaderBuilder> T customLoader(
			Supplier<T> factory,
			Consumer<T> configuration) {
		T builder = factory.get();
		configuration.accept(builder);
		return builder;
	}

	public ModelTemplate build() {
		return new ModelTemplate(parent, Optional.empty());
	}

	/**
	 * Builder for item display transforms.
	 */
	public static class TransformBuilder {
		public TransformBuilder rotation(float x, float y, float z) {
			return this;
		}

		public TransformBuilder translation(float x, float y, float z) {
			return this;
		}

		public TransformBuilder scale(float s) {
			return this;
		}

		public TransformBuilder scale(float x, float y, float z) {
			return this;
		}
	}
}
