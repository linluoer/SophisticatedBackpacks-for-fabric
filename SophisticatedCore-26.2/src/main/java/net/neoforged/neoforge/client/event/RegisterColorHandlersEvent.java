package net.neoforged.neoforge.client.event;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * Compatibility shim for {@code RegisterColorHandlersEvent}.
 * <p>
 * Mirrors the NeoForge split into {@link Block}, {@link Item},
 * {@link BlockTintSources} and {@link ItemTintSources} sub-events.
 * Item tint sources are registered directly into vanilla's
 * {@link ItemTintSources#ID_MAPPER} via reflection. Block tint source
 * registrations are buffered and applied later via
 * {@link #applyBlockTintSources(BlockColors)} once the client
 * {@link BlockColors} instance is available.
 */
public abstract class RegisterColorHandlersEvent extends Event {

	private static final List<BufferedBlockTintRegistration> BUFFERED_BLOCK_TINTS = new ArrayList<>();

	/** Fired to register {@link BlockColors} instances. */
	public static class Block extends RegisterColorHandlersEvent {
		/**
		 * Register a block color handler.
		 *
		 * @param color  the color handler
		 * @param blocks the blocks the handler applies to
		 */
		public void register(BlockColors color, net.minecraft.world.level.block.Block... blocks) {
			// no-op shim
		}
	}

	/** Fired to register item color handlers. */
	public static class Item extends RegisterColorHandlersEvent {
		/**
		 * Register an item color handler.
		 *
		 * @param color the color handler
		 * @param items the items the handler applies to
		 */
		public void register(BiFunction<ItemStack, Integer, Integer> color, net.minecraft.world.item.Item... items) {
			// no-op shim
		}
	}

	/** Fired to register block tint sources (data-driven block color handlers). */
	public static class BlockTintSources extends RegisterColorHandlersEvent {
		/**
		 * Register a list of block tint sources for the given blocks. The
		 * registration is buffered and applied to the client {@link BlockColors}
		 * instance once it becomes available.
		 *
		 * @param tintSources the block tint sources to register
		 * @param blocks      the blocks the tint sources apply to
		 */
		public void register(List<BlockTintSource> tintSources, net.minecraft.world.level.block.Block... blocks) {
			BUFFERED_BLOCK_TINTS.add(new BufferedBlockTintRegistration(List.copyOf(tintSources), Arrays.asList(blocks)));
		}
	}

	/** Fired to register item tint sources (data-driven item color handlers). */
	public static class ItemTintSources extends RegisterColorHandlersEvent {
		/**
		 * Register an item tint source under the given identifier. The
		 * registration is performed immediately against vanilla's
		 * {@link ItemTintSources#ID_MAPPER} via reflection.
		 *
		 * @param id       the identifier of the tint source
		 * @param mapCodec the map codec used to deserialize the tint source
		 */
		public void register(Identifier id, MapCodec<? extends ItemTintSource> mapCodec) {
			try {
				Class<?> clazz = net.minecraft.client.color.item.ItemTintSources.class;
				Field field = clazz.getDeclaredField("ID_MAPPER");
				field.setAccessible(true);
				Object mapper = field.get(null);
				Method putMethod = mapper.getClass().getMethod("put", Object.class, Object.class);
				putMethod.invoke(mapper, id, mapCodec);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Failed to register item tint source " + id, e);
			}
		}
	}

	/**
	 * Apply all buffered block tint source registrations to the given
	 * {@link BlockColors} instance. Should be called once the client
	 * {@link BlockColors} is available (e.g. on first client tick).
	 *
	 * @param blockColors the client block colors instance
	 */
	public static void applyBlockTintSources(BlockColors blockColors) {
		for (BufferedBlockTintRegistration reg : BUFFERED_BLOCK_TINTS) {
			blockColors.register(reg.tintSources(), reg.blocks().toArray(new net.minecraft.world.level.block.Block[0]));
		}
		BUFFERED_BLOCK_TINTS.clear();
	}

	private record BufferedBlockTintRegistration(List<BlockTintSource> tintSources, List<net.minecraft.world.level.block.Block> blocks) {
	}
}
