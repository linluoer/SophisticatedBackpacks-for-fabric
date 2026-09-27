package net.neoforged.neoforge.client.model.block;

/**
 * Compatibility shim for {@code CustomUnbakedBlockStateModel}.
 * <p>
 * Marker interface for custom unbaked block-state model implementations that
 * participate in the model-loading pipeline. The Fabric port is expected to
 * provide the concrete bridge to the underlying model system.
 */
public interface CustomUnbakedBlockStateModel {
}
