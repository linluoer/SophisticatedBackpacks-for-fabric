package net.neoforged.neoforge.transfer.transaction;

/**
 * 事务上下文 - 支持回滚的事务接口。
 * <p>
 * 通过 {@link #addCloseCallback} 注册回滚回调，在事务未提交而关闭时执行回滚。
 * 通过 {@link #addCommitCallback} 注册提交回调，在事务提交时执行。
 * <p>
 * {@link #OPEN_NOOP} 表示无事务上下文（操作立即生效），回滚回调为 no-op，提交回调立即执行。
 */
public interface TransactionContext {
	TransactionContext OPEN_NOOP = new TransactionContext() {
		@Override
		public void close() {
			// no-op - 立即提交，无需关闭
		}

		@Override
		public boolean isOpen() {
			return true;
		}

		@Override
		public TransactionContext getParent() {
			return null;
		}

		@Override
		public void addCloseCallback(Runnable callback) {
			// no-op - 立即提交模式下没有回滚
		}

		@Override
		public void addCommitCallback(Runnable callback) {
			// 立即执行提交回调
			callback.run();
		}
	};

	static TransactionContext createOpen() {
		return OPEN_NOOP;
	}

	void close();

	boolean isOpen();

	TransactionContext getParent();

	/**
	 * 注册回滚回调。当事务未提交而关闭（abort）时，按注册逆序执行回调。
	 * 对于 {@link #OPEN_NOOP}（立即提交模式），此方法为 no-op。
	 *
	 * @param callback 回滚时执行的回调
	 */
	void addCloseCallback(Runnable callback);

	/**
	 * 注册提交回调。当事务提交时执行。
	 * 对于 {@link #OPEN_NOOP}（立即提交模式），回调立即执行。
	 *
	 * @param callback 提交时执行的回调
	 */
	void addCommitCallback(Runnable callback);
}
