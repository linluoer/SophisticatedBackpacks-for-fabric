package net.p3pp3rf1y.sophisticatedcore.eventbus;

/**
 * 本移植版所有事件的基类。
 * <p>
 * 刻意放在本 mod 自己的命名空间下，而非 {@code net.p3pp3rf1y.sophisticatedcore.eventbus}：
 * CreativeCore 等 mod 会以嵌套 jar 形式引入真正的 NeoForge bus 7.2.0，
 * 其 {@code Event} 与本类同包同名时会发生类加载竞争。bus 7.2.0 的 {@code Event}
 * 不含 {@code isCanceled()}/{@code setCanceled()}（取消能力在 {@code ICancellableEvent}
 * 接口上），一旦它赢得类加载，本 mod 所有取消调用点都会抛 {@code NoSuchMethodError}。
 * 独立命名空间彻底消除该竞争。
 */
public class Event {
	private boolean canceled = false;

	public boolean isCancelable() {
		return true;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}
}
