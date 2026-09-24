package net.neoforged.neoforge.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/**
 * BaseFlowingFluid shim - extends FlowingFluid with NeoForge-style Properties.
 * Source and Flowing inner classes represent still and flowing variants.
 */
public abstract class BaseFlowingFluid extends FlowingFluid {
	protected final Properties properties;

	protected BaseFlowingFluid(Properties properties) {
		this.properties = properties;
	}

	public FluidType getFluidType() {
		return properties.fluidType.get();
	}

	@Override
	public Fluid getFlowing() {
		return properties.flowing.get();
	}

	@Override
	public Fluid getSource() {
		return properties.source.get();
	}

	@Override
	public Item getBucket() {
		return properties.bucket != null ? properties.bucket.get() : null;
	}

	@Override
	protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid other, Direction direction) {
		return false;
	}

	@Override
	public int getTickDelay(LevelReader level) {
		return 5;
	}

	@Override
	protected float getExplosionResistance() {
		return 100.0F;
	}

	@Override
	public float getOwnHeight(FluidState state) {
		return 0.0F;
	}

	@Override
	public float getHeight(FluidState state, BlockGetter level, BlockPos pos) {
		return 0.0F;
	}

	@Override
	protected BlockState createLegacyBlock(FluidState state) {
		return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
	}

	@Override
	public VoxelShape getShape(FluidState state, BlockGetter level, BlockPos pos) {
		return Shapes.empty();
	}

	@Override
	protected boolean canConvertToSource(ServerLevel level) {
		return false;
	}

	@Override
	protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
	}

	@Override
	protected int getSlopeFindDistance(LevelReader level) {
		return 4;
	}

	@Override
	protected int getDropOff(LevelReader level) {
		return 1;
	}

	public static class Source extends BaseFlowingFluid {
		public Source(Properties properties) {
			super(properties);
		}

		@Override
		public boolean isSource(FluidState state) {
			return true;
		}

		@Override
		public int getAmount(FluidState state) {
			return 8;
		}
	}

	public static class Flowing extends BaseFlowingFluid {
		public Flowing(Properties properties) {
			super(properties);
		}

		@Override
		protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}

		@Override
		public boolean isSource(FluidState state) {
			return false;
		}

		@Override
		public int getAmount(FluidState state) {
			return state.getValue(LEVEL);
		}
	}

	public static class Properties {
		private final Supplier<? extends FluidType> fluidType;
		private final Supplier<? extends Fluid> source;
		private final Supplier<? extends Fluid> flowing;
		private Supplier<? extends Item> bucket;

		public Properties(Supplier<? extends FluidType> fluidType, Supplier<? extends Fluid> source, Supplier<? extends Fluid> flowing) {
			this.fluidType = fluidType;
			this.source = source;
			this.flowing = flowing;
		}

		public Properties bucket(Supplier<? extends Item> bucket) {
			this.bucket = bucket;
			return this;
		}
	}
}
