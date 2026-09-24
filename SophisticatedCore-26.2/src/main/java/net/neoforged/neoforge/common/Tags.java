package net.neoforged.neoforge.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Tags shim - contains common tag constants using the c: (Fabric convention) prefix.
 */
public final class Tags {
	private Tags() {
	}

	public static final class Blocks {
		private Blocks() {
		}

		public static final TagKey<Block> STORAGE_BLOCKS = tag("storage_blocks");
		public static final TagKey<Block> STORAGE_BLOCKS_IRON = tag("storage_blocks/iron");
		public static final TagKey<Block> STORAGE_BLOCKS_GOLD = tag("storage_blocks/gold");
		public static final TagKey<Block> STORAGE_BLOCKS_COPPER = tag("storage_blocks/copper");
		public static final TagKey<Block> STORAGE_BLOCKS_NETHERITE = tag("storage_blocks/netherite");
		public static final TagKey<Block> STORAGE_BLOCKS_DIAMOND = tag("storage_blocks/diamond");
		public static final TagKey<Block> STORAGE_BLOCKS_EMERALD = tag("storage_blocks/emerald");
		public static final TagKey<Block> STORAGE_BLOCKS_LAPIS = tag("storage_blocks/lapis");
		public static final TagKey<Block> STORAGE_BLOCKS_REDSTONE = tag("storage_blocks/redstone");
		public static final TagKey<Block> STORAGE_BLOCKS_COAL = tag("storage_blocks/coal");
		public static final TagKey<Block> STORAGE_BLOCKS_RAW_IRON = tag("storage_blocks/raw_iron");
		public static final TagKey<Block> STORAGE_BLOCKS_RAW_GOLD = tag("storage_blocks/raw_gold");
		public static final TagKey<Block> STORAGE_BLOCKS_RAW_COPPER = tag("storage_blocks/raw_copper");
		public static final TagKey<Block> STORAGE_BLOCKS_QUARTZ = tag("storage_blocks/quartz");
		public static final TagKey<Block> STORAGE_BLOCKS_AMETHYST = tag("storage_blocks/amethyst");
		public static final TagKey<Block> ORES = tag("ores");
		public static final TagKey<Block> ORES_IRON = tag("ores/iron");
		public static final TagKey<Block> ORES_GOLD = tag("ores/gold");
		public static final TagKey<Block> ORES_COPPER = tag("ores/copper");
		public static final TagKey<Block> ORES_COAL = tag("ores/coal");
		public static final TagKey<Block> ORES_REDSTONE = tag("ores/redstone");
		public static final TagKey<Block> ORES_LAPIS = tag("ores/lapis");
		public static final TagKey<Block> ORES_DIAMOND = tag("ores/diamond");
		public static final TagKey<Block> ORES_EMERALD = tag("ores/emerald");
		public static final TagKey<Block> ORES_NETHERITE_SCRAP = tag("ores/netherite_scrap");
		public static final TagKey<Block> ORES_QUARTZ = tag("ores/quartz");

		private static TagKey<Block> tag(String name) {
			return TagKey.create(Registries.BLOCK, Identifier.parse("c:" + name));
		}
	}

	public static final class Items {
		private Items() {
		}

		public static final TagKey<Item> STORAGE_BLOCKS = tag("storage_blocks");
		public static final TagKey<Item> STORAGE_BLOCKS_IRON = tag("storage_blocks/iron");
		public static final TagKey<Item> STORAGE_BLOCKS_GOLD = tag("storage_blocks/gold");
		public static final TagKey<Item> STORAGE_BLOCKS_COPPER = tag("storage_blocks/copper");
		public static final TagKey<Item> STORAGE_BLOCKS_NETHERITE = tag("storage_blocks/netherite");
		public static final TagKey<Item> STORAGE_BLOCKS_DIAMOND = tag("storage_blocks/diamond");
		public static final TagKey<Item> STORAGE_BLOCKS_EMERALD = tag("storage_blocks/emerald");

		public static final TagKey<Item> STORAGE_BLOCKS_REDSTONE = tag("storage_blocks/redstone");

		public static final TagKey<Item> ORES = tag("ores");
		public static final TagKey<Item> ORES_IRON = tag("ores/iron");
		public static final TagKey<Item> ORES_GOLD = tag("ores/gold");
		public static final TagKey<Item> ORES_COPPER = tag("ores/copper");

		public static final TagKey<Item> INGOTS = tag("ingots");
		public static final TagKey<Item> INGOTS_IRON = tag("ingots/iron");
		public static final TagKey<Item> INGOTS_GOLD = tag("ingots/gold");
		public static final TagKey<Item> INGOTS_COPPER = tag("ingots/copper");
		public static final TagKey<Item> INGOTS_NETHERITE = tag("ingots/netherite");
		public static final TagKey<Item> INGOTS_BRICK = tag("ingots/brick");
		public static final TagKey<Item> INGOTS_NETHER_BRICK = tag("ingots/nether_brick");

		public static final TagKey<Item> NUGGETS = tag("nuggets");
		public static final TagKey<Item> NUGGETS_IRON = tag("nuggets/iron");
		public static final TagKey<Item> NUGGETS_GOLD = tag("nuggets/gold");

		public static final TagKey<Item> GEMS = tag("gems");
		public static final TagKey<Item> GEMS_DIAMOND = tag("gems/diamond");
		public static final TagKey<Item> GEMS_EMERALD = tag("gems/emerald");
		public static final TagKey<Item> GEMS_LAPIS = tag("gems/lapis");
		public static final TagKey<Item> GEMS_AMETHYST = tag("gems/amethyst");
		public static final TagKey<Item> GEMS_QUARTZ = tag("gems/quartz");

		public static final TagKey<Item> DYES = tag("dyes");
		public static final TagKey<Item> STRING = tag("string");
		public static final TagKey<Item> STRINGS = tag("strings");
		public static final TagKey<Item> LEATHER = tag("leather");
		public static final TagKey<Item> LEATHERS = tag("leathers");
		public static final TagKey<Item> FEATHERS = tag("feathers");
		public static final TagKey<Item> BONES = tag("bones");
		public static final TagKey<Item> EGGS = tag("eggs");
		public static final TagKey<Item> RODS = tag("rods");
		public static final TagKey<Item> RODS_BLAZE = tag("rods/blaze");
		public static final TagKey<Item> RODS_WOODEN = tag("rods/wooden");
		public static final TagKey<Item> SLIMEBALLS = tag("slimeballs");
		public static final TagKey<Item> NETHER_STARS = tag("nether_stars");
		public static final TagKey<Item> ENDER_PEARLS = tag("ender_pearls");
		public static final TagKey<Item> MUSIC_DISCS = tag("music_discs");
		public static final TagKey<Item> BUCKETS = tag("buckets");
		public static final TagKey<Item> BUCKETS_EMPTY = tag("buckets/empty");
		public static final TagKey<Item> BUCKETS_WATER = tag("buckets/water");
		public static final TagKey<Item> BUCKETS_LAVA = tag("buckets/lava");
		public static final TagKey<Item> BUCKETS_MILK = tag("buckets/milk");

		public static final TagKey<Item> DUSTS = tag("dusts");
		public static final TagKey<Item> DUSTS_REDSTONE = tag("dusts/redstone");
		public static final TagKey<Item> OBSIDIANS = tag("obsidians");
		public static final TagKey<Item> CHESTS = tag("chests");
		public static final TagKey<Item> CHESTS_WOODEN = tag("chests/wooden");
		public static final TagKey<Item> GLASS_BLOCKS = tag("glass_blocks");

		public static final TagKey<Item> TOOLS = tag("tools");
		public static final TagKey<Item> TOOLS_PICKAXES = tag("tools/pickaxes");
		public static final TagKey<Item> TOOLS_AXES = tag("tools/axes");
		public static final TagKey<Item> TOOLS_SHOVELS = tag("tools/shovels");
		public static final TagKey<Item> TOOLS_HOES = tag("tools/hoes");
		public static final TagKey<Item> TOOLS_SWORDS = tag("tools/swords");
		public static final TagKey<Item> TOOLS_FISHING_RODS = tag("tools/fishing_rods");
		public static final TagKey<Item> TOOLS_BOWS = tag("tools/bows");
		public static final TagKey<Item> TOOLS_CROSSBOWS = tag("tools/crossbows");
		public static final TagKey<Item> TOOLS_SHEARS = tag("tools/shears");
		/**
		 * 与 {@link #TOOLS_SHEARS} 等价的别名，兼容调用方使用单数形式的代码。
		 */
		public static final TagKey<Item> TOOLS_SHEAR = TOOLS_SHEARS;
		public static final TagKey<Item> TOOLS_SHIELDS = tag("tools/shields");
		public static final TagKey<Item> TOOLS_BRUSHES = tag("tools/brushes");
		public static final TagKey<Item> TOOLS_SPEARS = tag("tools/spears");

		public static final TagKey<Item> ARMORS = tag("armors");
		public static final TagKey<Item> ARMORS_HELMETS = tag("armors/helmets");
		public static final TagKey<Item> ARMORS_CHESTPLATES = tag("armors/chestplates");
		public static final TagKey<Item> ARMORS_LEGGINGS = tag("armors/leggings");
		public static final TagKey<Item> ARMORS_BOOTS = tag("armors/boots");

		public static final TagKey<Item> WEAPONS = tag("weapons");

		public static final TagKey<Item> RAW_MATERIALS = tag("raw_materials");
		public static final TagKey<Item> RAW_MATERIALS_IRON = tag("raw_materials/iron");
		public static final TagKey<Item> RAW_MATERIALS_GOLD = tag("raw_materials/gold");
		public static final TagKey<Item> RAW_MATERIALS_COPPER = tag("raw_materials/copper");

		private static TagKey<Item> tag(String name) {
			return TagKey.create(Registries.ITEM, Identifier.parse("c:" + name));
		}
	}

	public static final class Fluids {
		private Fluids() {
		}

		public static final TagKey<net.minecraft.world.level.material.Fluid> WATER = tag("water");
		public static final TagKey<net.minecraft.world.level.material.Fluid> LAVA = tag("lava");
		public static final TagKey<net.minecraft.world.level.material.Fluid> MILK = tag("milk");

		private static TagKey<net.minecraft.world.level.material.Fluid> tag(String name) {
			return TagKey.create(Registries.FLUID, Identifier.parse("c:" + name));
		}
	}

	public static final class EntityTypes {
		private EntityTypes() {
		}

		public static final TagKey<net.minecraft.world.entity.EntityType<?>> BOSSES = tag("bosses");
		/**
		 * 标记不可被捕捉的实体类型。NeoForge 中使用 neoforge: 命名空间，这里保留以保持兼容。
		 */
		public static final TagKey<net.minecraft.world.entity.EntityType<?>> CAPTURING_NOT_SUPPORTED =
				TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("neoforge:capturing_not_supported"));

		private static TagKey<net.minecraft.world.entity.EntityType<?>> tag(String name) {
			return TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("c:" + name));
		}
	}

	public static final class Biomes {
		private Biomes() {
		}

		public static final TagKey<net.minecraft.world.level.biome.Biome> IS_VOID = tag("is_void");
		public static final TagKey<net.minecraft.world.level.biome.Biome> IS_HOT = tag("is_hot");
		public static final TagKey<net.minecraft.world.level.biome.Biome> IS_COLD = tag("is_cold");

		private static TagKey<net.minecraft.world.level.biome.Biome> tag(String name) {
			return TagKey.create(Registries.BIOME, Identifier.parse("c:" + name));
		}
	}
}
