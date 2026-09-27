package net.neoforged.neoforge.transfer.resource;

/**
 * Resource alias - re-exports {@link net.neoforged.neoforge.transfer.Resource}
 * under the {@code transfer.resource} sub-package.
 * <p>
 * This is a type alias so that code importing
 * {@code net.neoforged.neoforge.transfer.resource.Resource} resolves to the
 * same {@code Resource} interface already defined at the parent package.
 * <p>
 * The interface itself is not directly instantiable; all usage should reference
 * the {@link net.neoforged.neoforge.transfer.Resource} interface type.
 */
public interface Resource extends net.neoforged.neoforge.transfer.Resource {
}
