package net.neoforged.neoforge.transfer;

import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * 资源处理器核心接口 - 管理资源的插入、提取和查询。
 * 简化实现，事务参数被忽略（直接提交）。
 *
 * @param <T> 资源类型
 */
public interface ResourceHandler<T> {
	int size();

	T getResource(int index);

	long getAmountAsLong(int index);

	default int getAmountAsInt(int index) {
		return (int) getAmountAsLong(index);
	}

	default int insert(T resource, int amount, TransactionContext transaction) {
		int inserted = 0;
		for (int i = 0; i < size() && inserted < amount; i++) {
			inserted += insert(i, resource, amount - inserted, transaction);
		}
		return inserted;
	}

	int insert(int index, T resource, int amount, TransactionContext transaction);

	default int extract(T resource, int amount, TransactionContext transaction) {
		int extracted = 0;
		for (int i = 0; i < size() && extracted < amount; i++) {
			extracted += extract(i, resource, amount - extracted, transaction);
		}
		return extracted;
	}

	int extract(int index, T resource, int amount, TransactionContext transaction);

	long getCapacityAsLong(int index, T resource);

	default int getCapacityAsInt(int index, T resource) {
		return (int) getCapacityAsLong(index, resource);
	}

	default boolean isValid(int index, T resource) {
		return true;
	}
}
