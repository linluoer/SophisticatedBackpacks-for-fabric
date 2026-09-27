package net.neoforged.neoforge.client.settings;

/**
 * Compatibility shim for {@code IKeyConflictContext}.
 * <p>
 * A conflict context describes the situations under which a key binding is
 * considered "active" and whether two contexts conflict with each other (so
 * that their key bindings should not coexist on the same key).
 */
public interface IKeyConflictContext {

	/**
	 * @return {@code true} if this context is currently active (i.e. its key
	 *         bindings should be considered for processing)
	 */
	boolean isActive();

	/**
	 * Determine whether this context conflicts with the supplied one.
	 *
	 * @param other another conflict context
	 * @return {@code true} if the two contexts conflict
	 */
	boolean conflicts(IKeyConflictContext other);
}
