package net.neoforged.neoforge.transfer.transaction;

import java.util.ArrayList;
import java.util.List;

public final class Transaction implements AutoCloseable, TransactionContext {
	private final List<Runnable> closeCallbacks = new ArrayList<>();
	private final List<Runnable> commitCallbacks = new ArrayList<>();
	private final TransactionContext parent;
	private boolean committed = false;
	private boolean closed = false;

	private Transaction(TransactionContext parent) {
		this.parent = parent;
	}

	public static Transaction openRoot() {
		return new Transaction(null);
	}

	public static Transaction open(TransactionContext parent) {
		return new Transaction(parent);
	}

	public TransactionContext getContext() {
		return this;
	}

	public void commit() {
		if (closed || committed) {
			return;
		}
		committed = true;
		if (parent == null) {
			for (Runnable callback : commitCallbacks) {
				callback.run();
			}
			commitCallbacks.clear();
			closeCallbacks.clear();
		} else {
			List<Runnable> rollbacks = new ArrayList<>(closeCallbacks);
			parent.addCloseCallback(() -> {
				for (int i = rollbacks.size() - 1; i >= 0; i--) {
					rollbacks.get(i).run();
				}
			});
			for (Runnable callback : commitCallbacks) {
				parent.addCommitCallback(callback);
			}
			commitCallbacks.clear();
			closeCallbacks.clear();
		}
	}

	public boolean isCommitted() {
		return committed;
	}

	@Deprecated
	public void abort() {
		close();
	}

	@Override
	public void close() {
		if (closed) {
			return;
		}
		closed = true;
		if (!committed) {
			for (int i = closeCallbacks.size() - 1; i >= 0; i--) {
				closeCallbacks.get(i).run();
			}
		}
		closeCallbacks.clear();
		commitCallbacks.clear();
	}

	@Override
	public void addCloseCallback(Runnable callback) {
		if (!closed && !committed) {
			closeCallbacks.add(callback);
		}
	}

	@Override
	public void addCommitCallback(Runnable callback) {
		if (!closed && !committed) {
			commitCallbacks.add(callback);
		}
	}

	@Override
	public boolean isOpen() {
		return !closed;
	}

	@Override
	public TransactionContext getParent() {
		return parent;
	}

	public static void execute(java.util.function.Consumer<TransactionContext> action) {
		Transaction tx = openRoot();
		try {
			action.accept(tx);
		} finally {
			tx.close();
		}
	}

	public static <T> T executeReturn(java.util.function.Function<TransactionContext, T> action) {
		Transaction tx = openRoot();
		try {
			return action.apply(tx);
		} finally {
			tx.close();
		}
	}
}
