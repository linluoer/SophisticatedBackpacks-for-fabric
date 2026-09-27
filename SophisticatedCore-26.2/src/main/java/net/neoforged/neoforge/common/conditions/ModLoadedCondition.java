package net.neoforged.neoforge.common.conditions;

import com.mojang.serialization.MapCodec;
import net.neoforged.fml.ModList;

/**
 * ModLoadedCondition shim - 表示某个 mod 已加载的条件。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。
 */
public class ModLoadedCondition implements ICondition {
	public static final MapCodec<ModLoadedCondition> CODEC = MapCodec.unit(new ModLoadedCondition(""));

	private final String modId;

	public ModLoadedCondition(String modId) {
		this.modId = modId;
	}

	public String getModId() {
		return modId;
	}

	@Override
	public boolean test(IContext context) {
		return ModList.get().isLoaded(modId);
	}

	@Override
	public MapCodec<? extends ICondition> codec() {
		return CODEC;
	}
}
