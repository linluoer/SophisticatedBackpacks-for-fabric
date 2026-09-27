package net.neoforged.neoforge.client.model.generators.blockstate;

/**
 * Functional interface for mutating unbaked block state models during data generation.
 * Fabric shim - no-op compatible with NeoForge's data generation API.
 */
@FunctionalInterface
public interface UnbakedMutator {
	void apply(Object unbaked);
}
