package net.p3pp3rf1y.sophisticatedbackpacks.init;

import net.neoforged.fml.ModList;
import net.p3pp3rf1y.sophisticatedbackpacks.SophisticatedBackpacks;
import net.p3pp3rf1y.sophisticatedcore.compat.CompatModIds;

public class ModCompat {
	private ModCompat() {
	}

	/**
	 * 初始化可选兼容模块。
	 * <p>
	 * 不走 {@code CompatRegistry}：Core 在自身构造期就调用了 {@code initCompats}，
	 * 早于 Backpacks 构造器执行到这里，注册进去的工厂不会再被实例化。
	 * 因此此处直接按 mod 是否加载来决定要不要启用。
	 */
	public static void register() {
		if (ModList.get().isLoaded(CompatModIds.TRINKETS)) {
			enable(CompatModIds.TRINKETS, TrinketsCompatLoader::setup);
		}
	}

	/**
	 * 惰性加载壳：对 {@code TrinketsCompat} 的引用集中在这个独立类里。
	 * <p>
	 * JVM 只在首次执行到 {@link #setup()} 时才解析该类及其引用的 trinkets API，
	 * 所以 trinkets 缺失时上面的 {@code isLoaded} 分支不进入，此类不被加载，
	 * 也就不会抛 {@code NoClassDefFoundError}。若把方法引用直接写在
	 * {@link #register()} 里，lambda 的具化会提前触发类解析，失去这层保护。
	 */
	private static final class TrinketsCompatLoader {
		private TrinketsCompatLoader() {
		}

		private static void setup() {
			new net.p3pp3rf1y.sophisticatedbackpacks.compat.trinkets.TrinketsCompat().setup();
		}
	}

	private static void enable(String modId, Runnable setup) {
		try {
			setup.run();
			SophisticatedBackpacks.LOGGER.info("Enabled {} compatibility", modId);
		} catch (Throwable t) {
			// 捕获 Throwable：第三方 API 版本不匹配时可能抛 NoSuchMethodError/NoClassDefFoundError，
			// 它们是 Error 而非 Exception。兼容模块失败不应影响背包本身。
			SophisticatedBackpacks.LOGGER.error("Failed to enable {} compatibility, skipping", modId, t);
		}
	}
}
