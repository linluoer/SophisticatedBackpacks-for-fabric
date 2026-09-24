package net.neoforged.neoforge.transfer;

import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * 空资源处理器单例 - 所有操作返回0/空。
 */
public class EmptyResourceHandler implements ResourceHandler<Object> {
	private static final EmptyResourceHandler INSTANCE = new EmptyResourceHandler();

	public static EmptyResourceHandler getInstance() {
		return INSTANCE;
	}

	@SuppressWarnings("unchecked")
	public static <T> ResourceHandler<T> instance() {
		return (ResourceHandler<T>) INSTANCE;
	}

	@SuppressWarnings("unchecked")
	public static <T> ResourceHandler<T> empty() {
		return (ResourceHandler<T>) INSTANCE;
	}

	private EmptyResourceHandler() {
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public Object getResource(int index) {
		return null;
	}

	@Override
	public long getAmountAsLong(int index) {
		return 0;
	}

	@Override
	public int insert(Object resource, int amount, TransactionContext transaction) {
		return 0;
	}

	@Override
	public int insert(int index, Object resource, int amount, TransactionContext transaction) {
		return 0;
	}

	@Override
	public int extract(Object resource, int amount, TransactionContext transaction) {
		return 0;
	}

	@Override
	public int extract(int index, Object resource, int amount, TransactionContext transaction) {
		return 0;
	}

	@Override
	public long getCapacityAsLong(int index, Object resource) {
		return 0;
	}

	@Override
	public boolean isValid(int index, Object resource) {
		return false;
	}
}
