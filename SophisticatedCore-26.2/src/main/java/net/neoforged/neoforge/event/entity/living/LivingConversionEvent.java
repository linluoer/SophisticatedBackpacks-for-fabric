package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * 生物转换事件 - 例如僵尸村民转化为村民。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。
 */
public abstract class LivingConversionEvent extends Event {
	private final LivingEntity original;
	private final LivingEntity outcome;

	protected LivingConversionEvent(LivingEntity original, LivingEntity outcome) {
		this.original = original;
		this.outcome = outcome;
	}

	public LivingEntity getOriginal() {
		return original;
	}

	public LivingEntity getOutcome() {
		return outcome;
	}

	/**
	 * 获取正在转换的实体（等同于 {@link #getOriginal()}）。
	 * 保留此方法以兼容 NeoForge API，SB 代码使用 {@code event.getEntity()} 获取转换前实体。
	 */
	public LivingEntity getEntity() {
		return original;
	}

	/**
	 * 转换前事件 - 可以取消转换。
	 */
	public static class Pre extends LivingConversionEvent {
		private boolean canceled;

		public Pre(LivingEntity original, LivingEntity outcome) {
			super(original, outcome);
		}

		@Override
		public boolean isCancelable() {
			return true;
		}

		@Override
		public boolean isCanceled() {
			return canceled;
		}

		@Override
		public void setCanceled(boolean canceled) {
			this.canceled = canceled;
		}
	}

	/**
	 * 转换后事件。
	 */
	public static class Post extends LivingConversionEvent {
		public Post(LivingEntity original, LivingEntity outcome) {
			super(original, outcome);
		}
	}
}
