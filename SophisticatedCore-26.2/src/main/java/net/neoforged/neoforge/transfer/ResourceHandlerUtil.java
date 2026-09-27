package net.neoforged.neoforge.transfer;

import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.function.Predicate;

/**
 * 资源处理器工具类。
 */
public final class ResourceHandlerUtil {
	private ResourceHandlerUtil() {
	}

	public static boolean isEmpty(ResourceHandler<?> handler) {
		if (handler == null || handler.size() == 0) {
			return true;
		}
		for (int i = 0; i < handler.size(); i++) {
			if (handler.getAmountAsLong(i) > 0) {
				return false;
			}
		}
		return true;
	}

	public static <T> ResourceStack<T> moveFirst(ResourceHandler<T> from, ResourceHandler<T> to, Predicate<T> filter, int maxAmount, TransactionContext tx) {
		int moved = 0;
		T movedResource = null;
		for (int i = 0; i < from.size() && moved < maxAmount; i++) {
			T resource = from.getResource(i);
			if (resource == null || (filter != null && !filter.test(resource))) {
				continue;
			}
			int available = from.getAmountAsInt(i);
			if (available <= 0) {
				continue;
			}
			int toMove = Math.min(available, maxAmount - moved);
			int extracted = from.extract(i, resource, toMove, tx);
			if (extracted > 0) {
				int inserted = to.insert(resource, extracted, tx);
				if (inserted < extracted) {
					// 回退未插入的部分
					from.insert(i, resource, extracted - inserted, tx);
				}
				moved += Math.min(inserted, extracted);
				if (movedResource == null) {
					movedResource = resource;
				}
			}
		}
		return movedResource != null ? new ResourceStack<>(movedResource, moved) : ResourceStack.empty();
	}

	public static <T> int moveAll(ResourceHandler<T> from, ResourceHandler<T> to, Predicate<T> filter, int maxAmount, TransactionContext tx) {
		int moved = 0;
		for (int i = 0; i < from.size() && moved < maxAmount; i++) {
			T resource = from.getResource(i);
			if (resource == null || (filter != null && !filter.test(resource))) {
				continue;
			}
			int available = from.getAmountAsInt(i);
			if (available <= 0) {
				continue;
			}
			int toMove = Math.min(available, maxAmount - moved);
			int extracted = from.extract(i, resource, toMove, tx);
			if (extracted > 0) {
				int inserted = to.insert(resource, extracted, tx);
				if (inserted < extracted) {
					from.insert(i, resource, extracted - inserted, tx);
				}
				moved += Math.min(inserted, extracted);
			}
		}
		return moved;
	}
}
