package net.p3pp3rf1y.sophisticatedcore.util;

import org.jspecify.annotations.Nullable;

/**
 * 客户端侧暂存菜单打开数据的持有者。
 * <p>
 * 服务端通过 {@code OpenMenuDataPayload} 发送额外数据，
 * 客户端 handler 将其存入此处，随后 {@code IContainerFactory.create(int, Inventory)}
 * 在构造菜单时消费。
 * <p>
 * 由于 {@code ClientboundOpenScreenPacket} 和本 payload 均在客户端主线程按序处理，
 * 数据保证在菜单创建前可用。使用后立即清空，避免泄漏。
 */
public final class OpenMenuDataHolder {
	@Nullable
	private static byte[] pendingData;

	private OpenMenuDataHolder() {
	}

	public static void setPendingData(byte[] data) {
		pendingData = data;
	}

	@Nullable
	public static byte[] consumePendingData() {
		byte[] data = pendingData;
		pendingData = null;
		return data;
	}
}
