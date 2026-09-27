package net.neoforged.neoforge.common.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 全局战利品修改器数据提供者 shim。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。子类需实现 {@link #start()} 方法，
 * 在其中通过 {@link #add(String, IGlobalLootModifier)} 注册修改器。
 * <p>
 * 本 shim 仅维护内存中的修改器列表，实际数据生成需由子类或外部 Fabric 数据生成器处理。
 */
public abstract class GlobalLootModifierProvider implements DataProvider {

	protected final PackOutput output;
	protected final CompletableFuture<HolderLookup.Provider> registries;
	protected final String modId;
	protected final Map<String, IGlobalLootModifier> modifiers = new LinkedHashMap<>();

	public GlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String modId) {
		this.output = output;
		this.registries = registries;
		this.modId = modId;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		start();
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public String getName() {
		return "Global Loot Modifiers: " + modId;
	}

	/**
	 * 子类实现此方法，在其中调用 {@link #add(String, IGlobalLootModifier)} 注册修改器。
	 */
	protected abstract void start();

	/**
	 * 添加一个全局战利品修改器。
	 *
	 * @param name     修改器名称（用作文件名）
	 * @param modifier 修改器实例
	 */
	protected void add(String name, IGlobalLootModifier modifier) {
		modifiers.put(name, modifier);
	}

	public String getModId() {
		return modId;
	}

	public Map<String, IGlobalLootModifier> getModifiers() {
		return modifiers;
	}
}
