package net.neoforged.neoforge.event.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * 方块破坏事件。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。
 */
public class BreakBlockEvent extends Event {
	private final Level level;
	private final BlockPos pos;
	private final BlockState state;
	private final Player player;
	private boolean canceled = false;
	private boolean notifyClient = false;

	public BreakBlockEvent(Level level, BlockPos pos, BlockState state, Player player) {
		this.level = level;
		this.pos = pos;
		this.state = state;
		this.player = player;
	}

	public Level getLevel() {
		return level;
	}

	public BlockPos getPos() {
		return pos;
	}

	public BlockState getState() {
		return state;
	}

	public Player getPlayer() {
		return player;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	/**
	 * 标记是否需要通知客户端。在 Fabric 简化实现中为 no-op，但保留 API 兼容。
	 */
	public void setNotifyClient(boolean notifyClient) {
		this.notifyClient = notifyClient;
	}

	public boolean isNotifyClient() {
		return notifyClient;
	}
}
