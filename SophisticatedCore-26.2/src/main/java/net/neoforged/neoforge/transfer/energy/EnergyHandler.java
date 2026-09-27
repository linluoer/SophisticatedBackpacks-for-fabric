package net.neoforged.neoforge.transfer.energy;

import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * 能量处理器接口 - 管理能量的插入和提取。
 */
public interface EnergyHandler {
	int insert(int amount, TransactionContext tx);

	int extract(int amount, TransactionContext tx);

	long getAmountAsLong();

	long getCapacityAsLong();

	default int getAmountAsInt() {
		return (int) getAmountAsLong();
	}

	default int getCapacityAsInt() {
		return (int) getCapacityAsLong();
	}

	default boolean canExtract() {
		return true;
	}

	default boolean canInsert() {
		return true;
	}
}
