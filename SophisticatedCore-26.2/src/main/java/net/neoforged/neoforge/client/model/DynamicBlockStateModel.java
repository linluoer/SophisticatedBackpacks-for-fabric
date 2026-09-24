package net.neoforged.neoforge.client.model;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Compatibility shim for {@code DynamicBlockStateModel}.
 * <p>
 * Implementations return a {@link BlockStateModel} computed dynamically from the
 * given block state, facing direction and random source. The Fabric port is
 * expected to bridge to its own dynamic model registration mechanism.
 */
public interface DynamicBlockStateModel {

	/**
	 * Return a baked model for the given block state.
	 *
	 * @param state   the block state being rendered
	 * @param facing  the directional facing, may be {@code null}
	 * @param random  the random source to use
	 * @return the baked model to render
	 */
	BlockStateModel getBakedModel(BlockState state, Direction facing, RandomSource random);
}
