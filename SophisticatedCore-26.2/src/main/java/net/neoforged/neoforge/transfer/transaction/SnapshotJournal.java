package net.neoforged.neoforge.transfer.transaction;

/**
 * 快照日志 - 用于事务回滚时保存/恢复状态。
 * <p>
 * {@link #updateSnapshots} 在首次调用时创建快照，并向事务注册回滚和提交回调：
 * <ul>
 *   <li>回滚（事务未提交而关闭）时调用 {@link #revertToSnapshot} 恢复快照</li>
 *   <li>提交时调用 {@link #onRootCommit} 并清除快照</li>
 * </ul>
 * 子类可重写 {@link #createSnapshot}、{@link #revertToSnapshot}、{@link #onRootCommit} 以支持特定逻辑。
 *
 * @param <T> 快照类型
 */
public abstract class SnapshotJournal<T> {
	private T originalState;
	private boolean registered = false;

	protected SnapshotJournal() {
	}

	public void updateSnapshots(TransactionContext transaction) {
		if (originalState == null) {
			originalState = createSnapshot();
		}
		if (!registered) {
			registered = true;
			transaction.addCloseCallback(() -> {
				if (originalState != null) {
					T state = originalState;
					originalState = null;
					revertToSnapshot(state);
				}
				registered = false;
			});
			transaction.addCommitCallback(() -> {
				if (originalState != null) {
					T state = originalState;
					originalState = null;
					onRootCommit(state);
				}
				registered = false;
			});
		}
	}

	public void commit() {
		if (originalState != null) {
			T state = originalState;
			originalState = null;
			onRootCommit(state);
		}
	}

	protected abstract T createSnapshot();

	protected void revertToSnapshot(T snapshot) {
		// 默认no-op，子类可重写
	}

	protected void onRootCommit(T originalState) {
		// 默认no-op，子类可重写
	}
}
