package net.neoforged.neoforge.network.codec;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

/**
 * Utility methods for creating stream codecs.
 * Shim for Fabric port - provides NeoForge-compatible stream codec factory methods.
 */
public final class NeoForgeStreamCodecs {
	private NeoForgeStreamCodecs() {
	}

	/**
	 * Creates a stream codec for an enum type using the enum class.
	 * The ordinal is written as a varint.
	 */
	public static <T extends Enum<T>> StreamCodec<FriendlyByteBuf, T> enumCodec(Class<T> enumClass) {
		T[] values = enumClass.getEnumConstants();
		return StreamCodec.of(
				(buf, value) -> buf.writeVarInt(value.ordinal()),
				buf -> values[buf.readVarInt()]
		);
	}

	/**
	 * Creates a stream codec for an enum type using a values supplier.
	 * The ordinal is written as a varint.
	 */
	public static <T extends Enum<T>> StreamCodec<FriendlyByteBuf, T> enumCodec(Supplier<T[]> valuesSupplier) {
		return StreamCodec.of(
				(buf, value) -> buf.writeVarInt(value.ordinal()),
				buf -> valuesSupplier.get()[buf.readVarInt()]
		);
	}

	/**
	 * Creates a stream codec from a DFU Codec.
	 * Delegates to ByteBufCodecs.fromCodec.
	 */
	public static <T> StreamCodec<ByteBuf, T> fromCodec(Codec<T> codec) {
		return ByteBufCodecs.fromCodec(codec);
	}

	/**
	 * Creates a stream codec for registry values that uses the registry's id mapper.
	 */
	public static <T> StreamCodec<RegistryFriendlyByteBuf, T> registryCodec(ResourceKey<? extends Registry<T>> registryKey) {
		return ByteBufCodecs.registry(registryKey);
	}

	/**
	 * Creates a stream codec for registry values from a specific registry.
	 */
	public static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromRegistry(Supplier<Registry<T>> registrySupplier) {
		return ByteBufCodecs.registry(registrySupplier.get().key());
	}
}
