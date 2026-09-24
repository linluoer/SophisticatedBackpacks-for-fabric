package net.p3pp3rf1y.sophisticatedcore.compat;

import net.p3pp3rf1y.sophisticatedcore.eventbus.IEventBus;
import net.neoforged.fml.ModContainer;

/**
 * 兼容层事件总线入口 - 提供 EVENT_BUS 静态字段，供源码引用 {@code ScNeoForge.EVENT_BUS}。
 * <p>
 * 原为 {@code net.neoforged.neoforge.common.NeoForge}，但该完整类名会被第三方 mod
 * （如异步粒子 AsyncParticles 的 Platform#createPlatform）作为 NeoForge 环境探测点
 * （{@code Class.forName("net.neoforged.neoforge.common.NeoForge")}），
 * 导致其在 Fabric 上误判为 NeoForge 并加载 NeoForge mixin 而崩溃。
 * 因此入口类必须避开 net.neoforged 包下可被探测的常见类名。
 * <p>
 * 实际事件触发由 Fabric 的事件 API 或调用方手动调用 {@code post()} 完成。
 */
public final class ScNeoForge {
	private ScNeoForge() {
	}

	/**
	 * 全局事件总线（GAME event bus），用于运行时事件（非 mod 加载阶段事件）。
	 */
	public static final IEventBus EVENT_BUS = ModContainer.createEventBus();
}
