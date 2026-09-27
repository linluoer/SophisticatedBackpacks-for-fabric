package net.neoforged.neoforge.client.fluid;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import java.util.HashMap;
import java.util.Map;

/**
 * Compatibility shim for {@code FluidTintSource}.
 * <p>
 * Provides a registry of tint sources per {@link Fluid} and exposes a tint color
 * for a given {@link FluidResource}. The port layer is expected to register
 * concrete tint sources at startup.
 */
public interface FluidTintSource {

	/** Registry of fluid -> tint source mappings. */
	Map<Fluid, FluidTintSource> TINT_SOURCES = new HashMap<>();

	/**
	 * Look up the tint source registered for the given fluid, falling back to a
	 * default white tint if none is registered.
	 *
	 * @param fluid the fluid to look up
	 * @return the registered tint source, or a default no-op source
	 */
	static FluidTintSource get(Fluid fluid) {
		FluidTintSource source = TINT_SOURCES.get(fluid);
		if (source != null) {
			return source;
		}
		return DEFAULT;
	}

	/** Default tint source that returns a fully opaque white color. */
	FluidTintSource DEFAULT = stack -> 0xFFFFFFFF;

	/**
	 * Compute the tint color for the given fluid resource.
	 *
	 * @param stack the fluid resource (analogous to vanilla {@code FluidStack})
	 * @return the packed ARGB tint color
	 */
	int getTintColor(FluidResource stack);

	/**
	 * 兼容 NeoForge 扩展 API：直接接受 {@link FluidStack}。
	 * <p>
	 * 默认实现将 FluidStack 转换为 FluidResource 后委托给 {@link #getTintColor(FluidResource)}。
	 *
	 * @param stack 流体堆
	 * @return 打包的 ARGB tint 颜色
	 */
	default int colorAsStack(FluidStack stack) {
		return getTintColor(FluidResource.of(stack));
	}
}
