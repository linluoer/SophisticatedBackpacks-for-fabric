package net.neoforged.neoforge.client.extensions.common;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;

/**
 * 客户端物品扩展接口 - 提供自定义渲染等客户端行为。
 */
public interface IClientItemExtensions {
	IClientItemExtensions DEFAULT = new IClientItemExtensions() {};

	default Object getCustomRenderer() {
		return null;
	}

	default HumanoidModel.ArmPose getArmPose(ItemStack stack, InteractionHand hand) {
		return null;
	}

	default boolean applyForgeHandTransform(net.minecraft.client.renderer.ItemInHandRenderer renderer,
			net.minecraft.client.player.LocalPlayer player, net.minecraft.world.InteractionHand hand,
			Object bufferSource, int packedLight,
			float partialTick, float interpolatedPitch, float swingProgress, float equipProgress,
			ItemStack stack) {
		return false;
	}
}
