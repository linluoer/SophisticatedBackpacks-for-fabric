package net.p3pp3rf1y.sophisticatedcore.eventbus;

/**
 * 事件优先级 - 兼容性 shim 用于 Fabric 移植。
 * <p>
 * 模仿 NeoForge EventPriority 枚举，用于在事件总线中按优先级排序监听器。
 * 在 shim 实现中，优先级可能不被严格遵守，但 API 表面保留。
 */
public enum EventPriority {
	/**
	 * 最高优先级，最先执行。
	 */
	HIGHEST,
	/**
	 * 高优先级。
	 */
	HIGH,
	/**
	 * 默认优先级。
	 */
	NORMAL,
	/**
	 * 低优先级。
	 */
	LOW,
	/**
	 * 最低优先级，最后执行。
	 */
	LOWEST
}
