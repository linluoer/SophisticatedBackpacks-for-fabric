package net.p3pp3rf1y.sophisticatedcore.compat;

import net.minecraft.nbt.CompoundTag;

/**
 * Interface injected into Entity via Mixin to provide NeoForge-compatible
 * {@code getPersistentData()} functionality.
 *
 * <p>The persistent data is a custom CompoundTag stored on each entity, used by mods
 * to attach arbitrary data. It is saved/loaded with the entity's NBT.</p>
 */
public interface IPersistentDataEntity {
	CompoundTag sophisticatedcore$getPersistentData();
}
