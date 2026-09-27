package net.neoforged.neoforge.transfer;

import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * 组合资源处理器 - 将多个ResourceHandler合并为一个。
 */
public class CombinedResourceHandler<T> implements ResourceHandler<T> {
	private final List<ResourceHandler<T>> handlers;

	@SafeVarargs
	public CombinedResourceHandler(ResourceHandler<T>... handlers) {
		this.handlers = List.of(handlers);
	}

	public CombinedResourceHandler(List<ResourceHandler<T>> handlers) {
		this.handlers = new ArrayList<>(handlers);
	}

	@Override
	public int size() {
		return handlers.stream().mapToInt(ResourceHandler::size).sum();
	}

	@Override
	public T getResource(int index) {
		int offset = 0;
		for (ResourceHandler<T> handler : handlers) {
			if (index < offset + handler.size()) {
				return handler.getResource(index - offset);
			}
			offset += handler.size();
		}
		return null;
	}

	@Override
	public long getAmountAsLong(int index) {
		int offset = 0;
		for (ResourceHandler<T> handler : handlers) {
			if (index < offset + handler.size()) {
				return handler.getAmountAsLong(index - offset);
			}
			offset += handler.size();
		}
		return 0;
	}

	@Override
	public int insert(T resource, int amount, TransactionContext transaction) {
		int moved = 0;
		for (ResourceHandler<T> handler : handlers) {
			if (moved >= amount) break;
			moved += handler.insert(resource, amount - moved, transaction);
		}
		return moved;
	}

	@Override
	public int insert(int index, T resource, int amount, TransactionContext transaction) {
		int offset = 0;
		for (ResourceHandler<T> handler : handlers) {
			if (index < offset + handler.size()) {
				return handler.insert(index - offset, resource, amount, transaction);
			}
			offset += handler.size();
		}
		return 0;
	}

	@Override
	public int extract(T resource, int amount, TransactionContext transaction) {
		int moved = 0;
		for (ResourceHandler<T> handler : handlers) {
			if (moved >= amount) break;
			moved += handler.extract(resource, amount - moved, transaction);
		}
		return moved;
	}

	@Override
	public int extract(int index, T resource, int amount, TransactionContext transaction) {
		int offset = 0;
		for (ResourceHandler<T> handler : handlers) {
			if (index < offset + handler.size()) {
				return handler.extract(index - offset, resource, amount, transaction);
			}
			offset += handler.size();
		}
		return 0;
	}

	@Override
	public long getCapacityAsLong(int index, T resource) {
		int offset = 0;
		for (ResourceHandler<T> handler : handlers) {
			if (index < offset + handler.size()) {
				return handler.getCapacityAsLong(index - offset, resource);
			}
			offset += handler.size();
		}
		return 0;
	}

	@Override
	public boolean isValid(int index, T resource) {
		int offset = 0;
		for (ResourceHandler<T> handler : handlers) {
			if (index < offset + handler.size()) {
				return handler.isValid(index - offset, resource);
			}
			offset += handler.size();
		}
		return false;
	}
}
