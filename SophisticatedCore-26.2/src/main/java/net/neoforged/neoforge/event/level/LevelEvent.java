package net.neoforged.neoforge.event.level;

import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * 关卡（Level/World）事件基类。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。
 */
public abstract class LevelEvent extends Event {
	private final Level level;

	protected LevelEvent(Level level) {
		this.level = level;
	}

	public Level getLevel() {
		return level;
	}

	/**
	 * 关卡加载完成事件。
	 */
	public static class Load extends LevelEvent {
		public Load(Level level) {
			super(level);
		}
	}

	/**
	 * 关卡卸载事件。
	 */
	public static class Unload extends LevelEvent {
		public Unload(Level level) {
			super(level);
		}
	}
}
