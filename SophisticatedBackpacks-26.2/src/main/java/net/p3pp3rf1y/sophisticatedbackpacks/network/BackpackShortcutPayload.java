package net.p3pp3rf1y.sophisticatedbackpacks.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.p3pp3rf1y.sophisticatedbackpacks.SophisticatedBackpacks;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlock;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;

public record BackpackShortcutPayload(BlockPos pos, Direction face, InteractionHand hand) implements CustomPacketPayload {
	public static final Type<BackpackShortcutPayload> TYPE = new Type<>(SophisticatedBackpacks.getIdentifier("backpack_shortcut"));
	public static final StreamCodec<ByteBuf, BackpackShortcutPayload> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC,
			BackpackShortcutPayload::pos, Direction.STREAM_CODEC, BackpackShortcutPayload::face, InteractionHand.STREAM_CODEC,
			BackpackShortcutPayload::hand, BackpackShortcutPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handlePayload(BackpackShortcutPayload payload, IPayloadContext context) {
		if (!(context.player() instanceof ServerPlayer player) || player.isSpectator() || !player.isAlive()
				|| player.containerMenu != player.inventoryMenu || !BackpackModifierPayload.isShortcutBound(player)
				|| !player.isWithinBlockInteractionRange(payload.pos(), 0)) {
			return;
		}
		ServerLevel level = player.level();
		if (!canModify(player, level, payload.pos())) {
			return;
		}
		HitResult target = player.pick(player.blockInteractionRange(), 1, false);
		if (!(target instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK
				|| !hit.getBlockPos().equals(payload.pos()) || hit.getDirection() != payload.face()) {
			return;
		}
		ItemStack heldItem = player.getItemInHand(payload.hand());
		if (player.getCooldowns().isOnCooldown(heldItem)) {
			return;
		}
		if (heldItem.getItem() instanceof BackpackItem backpackItem) {
			UseOnContext useContext = new UseOnContext(player, payload.hand(), hit);
			BlockPos placementPos = new BlockPlaceContext(useContext).getClickedPos();
			if (canModify(player, level, placementPos) && player.mayUseItemAt(placementPos, hit.getDirection(), heldItem)
					&& backpackItem.useOnShortcut(useContext).consumesAction()) {
				player.swing(payload.hand(), true);
				player.inventoryMenu.broadcastChanges();
			}
		} else if (heldItem.isEmpty() && player.mayBuild() && level.getBlockState(payload.pos()).getBlock() instanceof BackpackBlock) {
			BackpackBlock.pickupWithShortcut(level, payload.pos(), player, payload.hand());
			player.inventoryMenu.broadcastChanges();
		}
	}

	private static boolean canModify(ServerPlayer player, ServerLevel level, BlockPos pos) {
		return level.isLoaded(pos) && !level.isOutsideBuildHeight(pos) && level.getWorldBorder().isWithinBounds(pos)
				&& level.mayInteract(player, pos) && !level.getServer().isUnderSpawnProtection(level, pos, player);
	}
}
