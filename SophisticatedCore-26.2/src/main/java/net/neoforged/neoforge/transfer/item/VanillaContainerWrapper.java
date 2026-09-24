package net.neoforged.neoforge.transfer.item;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;

public class VanillaContainerWrapper implements ResourceHandler<ItemResource> {
	private static final ThreadLocal<StorageTransactionBridge> STORAGE_TRANSACTIONS = new ThreadLocal<>();
	private final Container container;
	@Nullable
	private final Direction side;

	private VanillaContainerWrapper(Container container, @Nullable Direction side) {
		this.container = container;
		this.side = side;
	}

	public static VanillaContainerWrapper of(Container container) {
		return new VanillaContainerWrapper(container, null);
	}

	public static VanillaContainerWrapper of(Container container, @Nullable Direction side) {
		return new VanillaContainerWrapper(container, side);
	}

	public static VanillaContainerWrapper of(SimpleContainer container) {
		return new VanillaContainerWrapper(container, null);
	}

	public static ResourceHandler<ItemResource> of(Storage<ItemVariant> storage) {
		return new ResourceHandler<>() {
			private List<StorageView<ItemVariant>> views() {
				List<StorageView<ItemVariant>> views = new ArrayList<>();
				storage.forEach(views::add);
				return views;
			}

			@Override
			public int size() {
				return views().size();
			}

			@Override
			public ItemResource getResource(int index) {
				StorageView<ItemVariant> view = views().get(index);
				return view.isResourceBlank() ? ItemResource.EMPTY : ItemResource.of(view.getResource().toStack());
			}

			@Override
			public long getAmountAsLong(int index) {
				return views().get(index).getAmount();
			}

			@Override
			public int insert(ItemResource resource, int amount, TransactionContext transaction) {
				if (resource.isEmpty() || amount <= 0) {
					return 0;
				}
				ItemVariant variant = ItemVariant.of(resource.toStack());
				return transfer(transaction, tx -> storage.insert(variant, amount, tx), amount);
			}

			@Override
			public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
				if (resource.isEmpty() || amount <= 0) {
					return 0;
				}
				StorageView<ItemVariant> view = views().get(index);
				if (view instanceof Storage<?> slotStorage) {
					@SuppressWarnings("unchecked")
					Storage<ItemVariant> itemSlot = (Storage<ItemVariant>) slotStorage;
					ItemVariant variant = ItemVariant.of(resource.toStack());
					return transfer(transaction, tx -> itemSlot.insert(variant, amount, tx), amount);
				}
				return 0;
			}

			@Override
			public int extract(ItemResource resource, int amount, TransactionContext transaction) {
				if (resource.isEmpty() || amount <= 0) {
					return 0;
				}
				ItemVariant variant = ItemVariant.of(resource.toStack());
				return transfer(transaction, tx -> storage.extract(variant, amount, tx), amount);
			}

			@Override
			public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
				if (resource.isEmpty() || amount <= 0) {
					return 0;
				}
				StorageView<ItemVariant> view = views().get(index);
				ItemVariant variant = ItemVariant.of(resource.toStack());
				return transfer(transaction, tx -> view.extract(variant, amount, tx), amount);
			}

			@Override
			public long getCapacityAsLong(int index, ItemResource resource) {
				return views().get(index).getCapacity();
			}
		};
	}

	private static int transfer(TransactionContext context, ToLongFunction<net.fabricmc.fabric.api.transfer.v1.transaction.Transaction> operation, int limit) {
		if (!context.isOpen() || context instanceof Transaction internal && internal.isCommitted()) {
			throw new IllegalStateException("Cannot transfer with a closed or committed transaction");
		}
		if (context == TransactionContext.OPEN_NOOP) {
			try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction tx = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()) {
				return performTransfer(operation, limit, tx);
			}
		}
		StorageTransactionBridge bridge = STORAGE_TRANSACTIONS.get();
		if (bridge == null) {
			bridge = new StorageTransactionBridge(rootContext(context));
		}
		return performTransfer(operation, limit, bridge.transactionFor(context).openNested());
	}

	private static int performTransfer(ToLongFunction<net.fabricmc.fabric.api.transfer.v1.transaction.Transaction> operation, int limit, net.fabricmc.fabric.api.transfer.v1.transaction.Transaction tx) {
		try (tx) {
			long moved = operation.applyAsLong(tx);
			if (moved > 0) {
				tx.commit();
			}
			return (int) Math.min(limit, moved);
		}
	}

	private static TransactionContext rootContext(TransactionContext context) {
		while (context.getParent() != null && context.getParent() != TransactionContext.OPEN_NOOP) {
			context = context.getParent();
		}
		return context;
	}

	private static final class StorageTransactionBridge {
		private final TransactionContext root;
		private final List<Frame> frames = new ArrayList<>();

		private StorageTransactionBridge(TransactionContext root) {
			this.root = root;
			net.fabricmc.fabric.api.transfer.v1.transaction.Transaction tx = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter();
			frames.add(new Frame(root, tx));
			try {
				root.addCloseCallback(this::abort);
				root.addCommitCallback(this::commit);
				STORAGE_TRANSACTIONS.set(this);
			} catch (RuntimeException | Error exception) {
				tx.close();
				throw exception;
			}
		}

		private net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transactionFor(TransactionContext context) {
			if (rootContext(context) != root) {
				throw new IllegalStateException("Another storage transaction is still open on this thread");
			}
			List<TransactionContext> missing = new ArrayList<>();
			TransactionContext ancestor = context;
			int index;
			while ((index = indexOf(ancestor)) < 0) {
				missing.add(ancestor);
				ancestor = ancestor.getParent();
			}
			while (frames.size() > index + 1) {
				Frame frame = frames.getLast();
				if (!(frame.context instanceof Transaction internal) || !internal.isCommitted()) {
					throw new IllegalStateException("A nested storage transaction is still open");
				}
				frame.transaction.commit();
				frames.removeLast();
			}
			for (int i = missing.size() - 1; i >= 0; i--) {
				TransactionContext child = missing.get(i);
				net.fabricmc.fabric.api.transfer.v1.transaction.Transaction tx = frames.getLast().transaction.openNested();
				Frame frame = new Frame(child, tx);
				try {
					child.addCloseCallback(() -> abortFrame(frame));
					frames.add(frame);
				} catch (RuntimeException | Error exception) {
					tx.close();
					throw exception;
				}
			}
			return frames.getLast().transaction;
		}

		private int indexOf(TransactionContext context) {
			for (int i = frames.size() - 1; i >= 0; i--) {
				if (frames.get(i).context == context) {
					return i;
				}
			}
			return -1;
		}

		private void abortFrame(Frame frame) {
			int index = indexOf(frame.context);
			if (index >= 0) {
				while (frames.size() > index) {
					frames.removeLast().transaction.close();
				}
			}
		}

		private void abort() {
			try {
				while (!frames.isEmpty()) {
					frames.removeLast().transaction.close();
				}
			} finally {
				STORAGE_TRANSACTIONS.remove();
			}
		}

		private void commit() {
			try {
				while (!frames.isEmpty()) {
					frames.removeLast().transaction.commit();
				}
			} finally {
				STORAGE_TRANSACTIONS.remove();
			}
		}

		private record Frame(TransactionContext context, net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transaction) {
		}
	}

	public static Storage<ItemVariant> toFabricStorage(Supplier<@Nullable ResourceHandler<ItemResource>> handlerSupplier) {
		return new Storage<>() {
			@Override
			public long insert(ItemVariant variant, long maxAmount, net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
				ResourceHandler<ItemResource> handler = handlerSupplier.get();
				if (handler == null || variant.isBlank() || maxAmount <= 0) {
					return 0;
				}
				Transaction tx = Transaction.openRoot();
				int inserted = handler.insert(ItemResource.of(variant.toStack()), (int) Math.min(maxAmount, Integer.MAX_VALUE), tx);
				if (inserted == 0) {
					tx.close();
					return 0;
				}
				attach(transaction, tx);
				return inserted;
			}

			@Override
			public long extract(ItemVariant variant, long maxAmount, net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
				ResourceHandler<ItemResource> handler = handlerSupplier.get();
				if (handler == null || variant.isBlank() || maxAmount <= 0) {
					return 0;
				}
				Transaction tx = Transaction.openRoot();
				int extracted = handler.extract(ItemResource.of(variant.toStack()), (int) Math.min(maxAmount, Integer.MAX_VALUE), tx);
				if (extracted == 0) {
					tx.close();
					return 0;
				}
				attach(transaction, tx);
				return extracted;
			}

			@Override
			public Iterator<StorageView<ItemVariant>> iterator() {
				ResourceHandler<ItemResource> handler = handlerSupplier.get();
				if (handler == null) {
					return List.<StorageView<ItemVariant>>of().iterator();
				}
				List<StorageView<ItemVariant>> views = new ArrayList<>();
				for (int i = 0; i < handler.size(); i++) {
					int index = i;
					views.add(new StorageView<>() {
						@Override
						public long extract(ItemVariant variant, long maxAmount, net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
							ResourceHandler<ItemResource> current = handlerSupplier.get();
							if (current == null || variant.isBlank() || maxAmount <= 0 || index >= current.size()) {
								return 0;
							}
							Transaction tx = Transaction.openRoot();
							int extracted = current.extract(index, ItemResource.of(variant.toStack()), (int) Math.min(maxAmount, Integer.MAX_VALUE), tx);
							attach(transaction, tx);
							return extracted;
						}

						@Override
						public boolean isResourceBlank() {
							return getResource().isBlank();
						}

						@Override
						public ItemVariant getResource() {
							ResourceHandler<ItemResource> current = handlerSupplier.get();
							return current == null || index >= current.size() ? ItemVariant.blank() : ItemVariant.of(current.getResource(index).toStack());
						}

						@Override
						public long getAmount() {
							ResourceHandler<ItemResource> current = handlerSupplier.get();
							return current == null || index >= current.size() ? 0 : current.getAmountAsLong(index);
						}

						@Override
						public long getCapacity() {
							ResourceHandler<ItemResource> current = handlerSupplier.get();
							return current == null || index >= current.size() ? 0 : current.getCapacityAsLong(index, current.getResource(index));
						}
					});
				}
				return views.iterator();
			}
		};
	}

	private static void attach(net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext fabricTx, Transaction tx) {
		fabricTx.addCloseCallback((context, result) -> {
			if (result.wasAborted()) {
				tx.close();
			}
		});
		fabricTx.addOuterCloseCallback(result -> {
			if (result.wasCommitted()) {
				tx.commit();
			}
			tx.close();
		});
	}

	@Override
	public int size() {
		return slots().length;
	}

	private int[] slots() {
		if (side != null && container instanceof WorldlyContainer worldly) {
			return worldly.getSlotsForFace(side);
		}
		int[] slots = new int[container.getContainerSize()];
		for (int i = 0; i < slots.length; i++) {
			slots[i] = i;
		}
		return slots;
	}

	private int actualSlot(int index) {
		return slots()[index];
	}

	@Override
	public ItemResource getResource(int index) {
		return ItemResource.of(container.getItem(actualSlot(index)));
	}

	@Override
	public long getAmountAsLong(int index) {
		return container.getItem(actualSlot(index)).getCount();
	}

	@Override
	public int getAmountAsInt(int index) {
		return container.getItem(actualSlot(index)).getCount();
	}

	private boolean canInsert(int index, ItemStack stack) {
		int slot = actualSlot(index);
		return container.canPlaceItem(slot, stack) && (side == null || !(container instanceof WorldlyContainer worldly) || worldly.canPlaceItemThroughFace(slot, stack, side));
	}

	private boolean canExtract(int index, ItemStack stack) {
		return side == null || !(container instanceof WorldlyContainer worldly) || worldly.canTakeItemThroughFace(actualSlot(index), stack, side);
	}

	private void snapshot(TransactionContext transaction) {
		List<ItemStack> before = new ArrayList<>();
		for (int i = 0; i < container.getContainerSize(); i++) {
			before.add(container.getItem(i).copy());
		}
		transaction.addCloseCallback(() -> {
			for (int i = 0; i < before.size(); i++) {
				container.setItem(i, before.get(i));
			}
			container.setChanged();
		});
	}

	@Override
	public int insert(ItemResource resource, int amount, TransactionContext transaction) {
		int moved = 0;
		for (int i = 0; i < size() && moved < amount; i++) {
			if (!container.getItem(actualSlot(i)).isEmpty()) {
				moved += insert(i, resource, amount - moved, transaction);
			}
		}
		for (int i = 0; i < size() && moved < amount; i++) {
			if (container.getItem(actualSlot(i)).isEmpty()) {
				moved += insert(i, resource, amount - moved, transaction);
			}
		}
		return moved;
	}

	@Override
	public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
		if (index < 0 || index >= size() || resource.isEmpty() || amount <= 0 || !canInsert(index, resource.toStack())) {
			return 0;
		}
		int slot = actualSlot(index);
		ItemStack existing = container.getItem(slot);
		if (!existing.isEmpty() && !resource.matches(existing)) {
			return 0;
		}
		int limit = Math.min(container.getMaxStackSize(resource.toStack()), resource.getMaxStackSize());
		int count = Math.min(amount, limit - existing.getCount());
		if (count <= 0) {
			return 0;
		}
		snapshot(transaction);
		container.setItem(slot, resource.toStack(existing.getCount() + count));
		container.setChanged();
		return count;
	}

	@Override
	public int extract(ItemResource resource, int amount, TransactionContext transaction) {
		int moved = 0;
		for (int i = 0; i < size() && moved < amount; i++) {
			moved += extract(i, resource, amount - moved, transaction);
		}
		return moved;
	}

	@Override
	public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
		if (index < 0 || index >= size() || resource.isEmpty() || amount <= 0) {
			return 0;
		}
		int slot = actualSlot(index);
		ItemStack existing = container.getItem(slot);
		if (existing.isEmpty() || !resource.matches(existing) || !canExtract(index, existing)) {
			return 0;
		}
		int count = Math.min(existing.getCount(), amount);
		snapshot(transaction);
		container.setItem(slot, existing.copyWithCount(existing.getCount() - count));
		container.setChanged();
		return count;
	}

	@Override
	public long getCapacityAsLong(int index, ItemResource resource) {
		return resource.isEmpty() ? container.getMaxStackSize() : Math.min(container.getMaxStackSize(resource.toStack()), resource.getMaxStackSize());
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return index >= 0 && index < size() && !resource.isEmpty() && canInsert(index, resource.toStack());
	}

	public Container getContainer() {
		return container;
	}
}
