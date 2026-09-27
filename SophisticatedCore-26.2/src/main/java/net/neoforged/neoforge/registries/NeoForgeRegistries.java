package net.neoforged.neoforge.registries;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * NeoForge 自定义注册表的 ResourceKey 集合。
 * <p>
 * 兼容性 shim：仅定义 ResourceKey 用于 DeferredRegister.create 调用。
 * 在 Fabric 移植中，对应的实际注册表由专用 shim（例如 FluidTypeLookup）处理，
 * 而不是通过 Minecraft 的 BuiltInRegistries 注册。
 */
public final class NeoForgeRegistries {
	private NeoForgeRegistries() {
	}

	/**
	 * FluidType 注册表的 ResourceKey。
	 */
	public static final ResourceKey<Registry<FluidType>> FLUID_TYPES = ResourceKey.createRegistryKey(Identifier.parse("neoforge:fluid_type"));

	/**
	 * 数据组件注册表的 ResourceKey（如果需要）。
	 */
	public static final ResourceKey<Registry<DataComponentType<?>>> DATA_COMPONENTS = ResourceKey
			.createRegistryKey(Identifier.parse("neoforge:data_component_type"));

	/**
	 * 全局战利品修改器序列化器注册表的 ResourceKey。
	 */
	public static final ResourceKey<Registry<MapCodec<? extends IGlobalLootModifier>>> GLOBAL_LOOT_MODIFIER_SERIALIZERS = ResourceKey
			.createRegistryKey(Identifier.parse("neoforge:global_loot_modifier_serializers"));

	/**
	 * 数据附件类型注册表的 ResourceKey。
	 */
	public static final ResourceKey<Registry<AttachmentType<?>>> ATTACHMENT_TYPES = ResourceKey
			.createRegistryKey(Identifier.parse("neoforge:attachment_type"));

	/**
	 * NeoForge 各类注册表 ResourceKey 的 Keys 内部类。
	 * 与 NeoForge 源码中的 NeoForgeRegistries.Keys 保持一致。
	 */
	public static final class Keys {
		private Keys() {
		}

		public static final ResourceKey<Registry<FluidType>> FLUID_TYPES = NeoForgeRegistries.FLUID_TYPES;

		public static final ResourceKey<Registry<MapCodec<? extends ICondition>>> CONDITION_CODECS = ResourceKey
				.createRegistryKey(Identifier.parse("neoforge:condition_codecs"));

		public static final ResourceKey<Registry<MapCodec<? extends IGlobalLootModifier>>> GLOBAL_LOOT_MODIFIER_SERIALIZERS = NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS;

		public static final ResourceKey<Registry<AttachmentType<?>>> ATTACHMENT_TYPES = NeoForgeRegistries.ATTACHMENT_TYPES;
	}
}
