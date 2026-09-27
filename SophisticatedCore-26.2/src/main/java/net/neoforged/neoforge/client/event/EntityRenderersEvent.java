package net.neoforged.neoforge.client.event;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * Compatibility shim for the NeoForge {@code EntityRenderersEvent} hierarchy.
 * <p>
 * This stub provides the base class plus the {@link RegisterRenderers}
 * sub-event used by listeners that need to register entity and block-entity
 * renderers. Registrations bridge to vanilla {@link EntityRenderers#register}
 * and {@link BlockEntityRenderers#register}, both of which are access-widened
 * by {@code fabric-transitive-access-wideners-v1}.
 */
public abstract class EntityRenderersEvent extends Event {

	/**
	 * Fired to allow mods to register entity and block-entity renderers. The
	 * shim bridges to vanilla's renderer registration mechanism.
	 */
	public static class RegisterRenderers extends EntityRenderersEvent {

		/**
		 * Register an entity renderer for the given entity type.
		 *
		 * @param entityType the entity type to register a renderer for
		 * @param provider   the entity renderer provider
		 * @param <T>        the entity type
		 */
		public <T extends net.minecraft.world.entity.Entity> void registerEntityRenderer(EntityType<? extends T> entityType,
				EntityRendererProvider<T> provider) {
			EntityRenderers.register(entityType, provider);
		}

		/**
		 * Register a block-entity renderer for the given block-entity type.
		 *
		 * @param blockEntityType the block-entity type to register a renderer for
		 * @param provider        the block-entity renderer provider
		 * @param <T>             the block-entity type
		 * @param <S>             the render state type
		 */
		public <T extends net.minecraft.world.level.block.entity.BlockEntity, S extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState> void registerBlockEntityRenderer(
				BlockEntityType<? extends T> blockEntityType, BlockEntityRendererProvider<T, S> provider) {
			BlockEntityRenderers.register(blockEntityType, provider);
		}
	}

	/**
	 * Fired to allow mods to register layer definitions for entity renderers.
	 * The shim is a stub that exposes the registration method used by listeners.
	 */
	public static class RegisterLayerDefinitions extends EntityRenderersEvent {

		/**
		 * Register a layer definition under the given identifier.
		 *
		 * @param id the identifier of the layer definition
		 */
		public void registerLayerDefinition(Identifier id) {
			// no-op shim
		}
	}
}
