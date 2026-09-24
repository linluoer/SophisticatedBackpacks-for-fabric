package net.neoforged.neoforge.event.tick;

import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * 关卡 tick 事件基类。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。
 */
public abstract class LevelTickEvent extends Event {
	private final Level level;
	private final boolean isClient;

	protected LevelTickEvent(Level level, boolean isClient) {
		this.level = level;
		this.isClient = isClient;
	}

	public Level getLevel() {
		return level;
	}

	public boolean isClient() {
		return isClient;
	}

	/**
	 * 关卡 tick 前事件。
	 */
	public static class Pre extends LevelTickEvent {
		public Pre(Level level) {
			super(level, level.isClientSide());
		}

		public Pre(Level level, boolean isClient) {
			super(level, isClient);
		}
	}

	/**
	 * 关卡 tick 后事件。
	 */
	public static class Post extends LevelTickEvent {
		public Post(Level level) {
			super(level, level.isClientSide());
		}

		public Post(Level level, boolean isClient) {
			super(level, isClient);
		}
	}
}
