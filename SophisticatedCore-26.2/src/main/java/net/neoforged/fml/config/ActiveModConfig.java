package net.neoforged.fml.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Represents an active (loaded) mod configuration. Simplified shim for Fabric port.
 * 不再依赖 ModConfig 类实例，避免与 ForgeConfigAPIPort 的 ModConfig 类加载冲突。
 * 直接存储 type 和 spec，保证无论哪个 ModConfig 类被加载都不崩溃。
 */
public class ActiveModConfig {
	private final ModConfig.Type type;
	private final ModConfigSpec spec;

	public ActiveModConfig(ModConfig.Type type, ModConfigSpec spec) {
		this.type = type;
		this.spec = spec;
	}

	public ModConfig.Type getType() {
		return type;
	}

	public ModConfigSpec getSpec() {
		return spec;
	}

	public void save() {
		// No-op: simplified shim does not persist to file
	}

	public void load() {
		// No-op: simplified shim does not load from file
	}
}
