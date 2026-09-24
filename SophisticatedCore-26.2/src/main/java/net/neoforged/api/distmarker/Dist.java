package net.neoforged.api.distmarker;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Compatibility shim for NeoForge's {@code Dist} enumeration.
 * <p>
 * Marks a logical distribution side - either the integrated/combined client
 * or a dedicated server. Used to annotate code that should only execute on a
 * particular side, mirroring {@code @OnlyIn(Dist.CLIENT)} semantics.
 */
public enum Dist {
	CLIENT,
	DEDICATED_SERVER;

	/**
	 * @return {@code true} if this distribution is the client
	 */
	public boolean isClient() {
		return this == CLIENT;
	}

	/**
	 * @return {@code true} if this distribution is a dedicated server
	 */
	public boolean isDedicatedServer() {
		return this == DEDICATED_SERVER;
	}

	/**
	 * Alias for {@link #isDedicatedServer()}.
	 */
	public boolean isDedicated() {
		return isDedicatedServer();
	}

	/**
	 * @return the current distribution side based on the Fabric environment
	 */
	public static Dist current() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? CLIENT : DEDICATED_SERVER;
	}
}
