package net.neoforged.neoforge.attachment;

import com.mojang.serialization.Codec;

import java.util.function.Supplier;

/**
 * 数据附件类型 - 用于在实体/方块实体上存储自定义数据。
 * 简化实现：在Fabric中使用DataComponentType或自定义NBT处理。
 *
 * @param <T> 附件数据类型
 */
public class AttachmentType<T> {
	private final Supplier<T> defaultSupplier;
	private final Codec<T> codec;
	private final boolean copyOnDeath;

	private AttachmentType(Supplier<T> defaultSupplier, Codec<T> codec, boolean copyOnDeath) {
		this.defaultSupplier = defaultSupplier;
		this.codec = codec;
		this.copyOnDeath = copyOnDeath;
	}

	public T defaultValue() {
		return defaultSupplier.get();
	}

	public Codec<T> codec() {
		return codec;
	}

	public boolean shouldCopyOnDeath() {
		return copyOnDeath;
	}

	public static <T> Builder<T> builder(Supplier<T> defaultSupplier) {
		return new Builder<>(defaultSupplier);
	}

	public static class Builder<T> {
		private final Supplier<T> defaultSupplier;
		private Codec<T> codec;
		private boolean copyOnDeath = false;

		private Builder(Supplier<T> defaultSupplier) {
			this.defaultSupplier = defaultSupplier;
		}

		public Builder<T> serialize(Codec<T> codec) {
			this.codec = codec;
			return this;
		}

		public Builder<T> copyOnDeath() {
			this.copyOnDeath = true;
			return this;
		}

		public AttachmentType<T> build() {
			return new AttachmentType<>(defaultSupplier, codec, copyOnDeath);
		}
	}
}
