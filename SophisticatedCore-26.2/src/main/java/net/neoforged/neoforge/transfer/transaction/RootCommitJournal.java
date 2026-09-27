package net.neoforged.neoforge.transfer.transaction;

/**
 * 根提交日志 - 简化实现。
 * <p>
 * NeoForge 中 RootCommitJournal 接受一个 onCommit 回调，在事务根提交时被调用。
 * 这里通过 {@link #onCommit} 字段保存回调，在 {@link #commit()} 时执行。
 */
public class RootCommitJournal<T> extends SnapshotJournal<T> {

	private final Runnable onCommit;

	/**
	 * 默认构造器：无提交回调。
	 */
	public RootCommitJournal() {
		this.onCommit = null;
	}

	/**
	 * 接受一个提交回调的构造器。在 {@link #commit()} 时会调用该回调。
	 *
	 * @param onCommit 根提交时执行的回调
	 */
	public RootCommitJournal(Runnable onCommit) {
		this.onCommit = onCommit;
	}

	@Override
	protected T createSnapshot() {
		return null;
	}

	@Override
	public void commit() {
		super.commit();
		if (onCommit != null) {
			onCommit.run();
		}
	}
}
