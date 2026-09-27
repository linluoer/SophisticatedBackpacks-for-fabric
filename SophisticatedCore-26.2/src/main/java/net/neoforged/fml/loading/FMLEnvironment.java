package net.neoforged.fml.loading;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.neoforged.api.distmarker.Dist;

/**
 * Provides information about the current FML environment.
 * Shim for Fabric port - delegates to FabricLoader for dist detection.
 * <p>
 * 注意：不能调用 {@code Dist.current()}，因为 CreativeCore 提供的 Dist 类（abstract enum）
 * 会被优先加载，而它没有 {@code current()} 静态方法。也不能调用 {@code dist.isDedicated()}，
 * 因为 CreativeCore 的 Dist 类没有这个方法。改用 FabricLoader 直接判断环境，
 * 并用 {@code dist.isDedicatedServer()} 代替。
 */
public class FMLEnvironment {
	public static final Dist dist = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? Dist.CLIENT : Dist.DEDICATED_SERVER;
	public static final boolean dedicated = dist.isDedicatedServer();

	public static Dist getDist() {
		return dist;
	}

	public static boolean isClient() {
		return dist.isClient();
	}

	public static boolean isDedicatedServer() {
		return dist.isDedicatedServer();
	}
}
