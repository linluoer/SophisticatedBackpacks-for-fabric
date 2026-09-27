package net.neoforged.fml.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Represents a mod configuration. Simplified shim for Fabric port.
 */
public class ModConfig {
	public enum Type {
		COMMON,
		CLIENT,
		SERVER,
		STARTUP
	}

	private final Type type;
	private final ModConfigSpec spec;
	private final String fileName;
	private final ModContainer modContainer;

	public ModConfig(Type type, ModConfigSpec spec, ModContainer modContainer, String fileName) {
		this.type = type;
		this.spec = spec;
		this.modContainer = modContainer;
		this.fileName = fileName;
	}

	public ModConfig(Type type, ModConfigSpec spec, ModContainer modContainer) {
		this(type, spec, modContainer, null);
	}

	public Type getType() {
		return type;
	}

	public ModConfigSpec getSpec() {
		return spec;
	}

	public String getFileName() {
		return fileName;
	}

	public ModContainer getModContainer() {
		return modContainer;
	}
}
