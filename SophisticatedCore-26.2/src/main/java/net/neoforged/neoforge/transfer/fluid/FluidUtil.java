package net.neoforged.neoforge.transfer.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * FluidUtil shim in transfer.fluid package - re-exports common fluid utility
 * operations. The Fabric port provides simplified stub implementations.
 */
public final class FluidUtil {
	private FluidUtil() {
	}

	public static boolean tryPlaceFluid(FluidResource resource, @Nullable Object player, Level level, InteractionHand hand, BlockPos pos) {
		return false;
	}

	public static boolean interactWithFluidHandler(Player player, InteractionHand hand, Level level, BlockPos pos, Direction direction) {
		return false;
	}

	public static void triggerSoundAndGameEvent(FluidResource resource, Level level, Vec3 pos, Player player, boolean pickup) {
	}
}
