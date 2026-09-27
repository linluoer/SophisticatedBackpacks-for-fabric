package net.neoforged.neoforge.client.model.item;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;

/**
 * 动态流体容器模型 - 用于流体容器物品的渲染。
 * 简化实现。
 */
public class DynamicFluidContainerModel {
	private final Identifier fluidLocation;

	public DynamicFluidContainerModel(Identifier fluidLocation) {
		this.fluidLocation = fluidLocation;
	}

	public Identifier getFluidLocation() {
		return fluidLocation;
	}

	public TextureAtlasSprite getParticleIcon(ModelManager modelManager) {
		return null;
	}

	public static BlockStateModel create(Fluid fluid) {
		return null;
	}
}
