package net.neoforged.neoforge.event.entity.player;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * 玩家交互事件基类。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。
 */
public abstract class PlayerInteractEvent extends Event {
	private final Player player;
	private final InteractionHand hand;
	private final BlockPos pos;
	private final Direction face;
	private InteractionResult cancellationResult = InteractionResult.PASS;

	protected PlayerInteractEvent(Player player, InteractionHand hand, BlockPos pos, Direction face) {
		this.player = player;
		this.hand = hand;
		this.pos = pos;
		this.face = face;
	}

	public Player getEntity() {
		return player;
	}

	public Player getPlayer() {
		return player;
	}

	public InteractionHand getHand() {
		return hand;
	}

	public BlockPos getPos() {
		return pos;
	}

	public Direction getFace() {
		return face;
	}

	public ItemStack getItemStack() {
		return player != null ? player.getItemInHand(hand) : ItemStack.EMPTY;
	}

	public Level getLevel() {
		return player != null ? player.level() : null;
	}

	public InteractionResult getCancellationResult() {
		return cancellationResult;
	}

	public void setCancellationResult(InteractionResult result) {
		this.cancellationResult = result;
	}

	@Override
	public boolean isCancelable() {
		return true;
	}

	/**
	 * 玩家右键点击方块事件。
	 */
	public static class RightClickBlock extends PlayerInteractEvent {
		private final ItemStack itemStack;

		public RightClickBlock(Player player, InteractionHand hand, BlockPos pos, Direction face) {
			super(player, hand, pos, face);
			this.itemStack = player != null ? player.getItemInHand(hand) : ItemStack.EMPTY;
		}

		public RightClickBlock(Player player, ItemStack stack, InteractionHand hand, BlockPos pos, Direction face) {
			super(player, hand, pos, face);
			this.itemStack = stack;
		}

		@Override
		public ItemStack getItemStack() {
			return itemStack;
		}
	}

	/**
	 * 玩家右键点击空气事件。
	 */
	public static class RightClickEmpty extends PlayerInteractEvent {
		public RightClickEmpty(Player player, InteractionHand hand) {
			super(player, hand, BlockPos.ZERO, null);
		}
	}

	/**
	 * 玩家左键点击方块事件。
	 */
	public static class LeftClickBlock extends PlayerInteractEvent {
		public LeftClickBlock(Player player, InteractionHand hand, BlockPos pos, Direction face) {
			super(player, hand, pos, face);
		}
	}

	/**
	 * 玩家与实体特定位置交互事件。
	 */
	public static class EntityInteractSpecific extends PlayerInteractEvent {
		private final Entity target;
		private final Vec3 localPos;

		public EntityInteractSpecific(Player player, InteractionHand hand, Entity target, Vec3 localPos) {
			super(player, hand, BlockPos.ZERO, null);
			this.target = target;
			this.localPos = localPos;
		}

		public Entity getTarget() {
			return target;
		}

		public Vec3 getLocalPos() {
			return localPos;
		}
	}
}
