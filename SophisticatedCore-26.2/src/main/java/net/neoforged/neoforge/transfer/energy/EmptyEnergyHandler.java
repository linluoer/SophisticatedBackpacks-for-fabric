package net.neoforged.neoforge.transfer.energy;

import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * 空能量处理器 - 所有操作返回0。
 */
public class EmptyEnergyHandler implements EnergyHandler {
	public static final EmptyEnergyHandler INSTANCE = new EmptyEnergyHandler();

	public static EmptyEnergyHandler getInstance() {
		return INSTANCE;
	}

	private EmptyEnergyHandler() {
	}

	@Override
	public int insert(int amount, TransactionContext tx) {
		return 0;
	}

	@Override
	public int extract(int amount, TransactionContext tx) {
		return 0;
	}

	@Override
	public long getAmountAsLong() {
		return 0;
	}

	@Override
	public long getCapacityAsLong() {
		return 0;
	}

	@Override
	public boolean canExtract() {
		return false;
	}

	@Override
	public boolean canInsert() {
		return false;
	}
}
