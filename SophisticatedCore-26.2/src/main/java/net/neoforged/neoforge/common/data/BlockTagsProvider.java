package net.neoforged.neoforge.common.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

/**
 * Block tags provider shim for Fabric.
 * Extends TagsProvider&lt;Block&gt; with the BLOCK registry key.
 */
public abstract class BlockTagsProvider extends TagsProvider<Block> {
	public BlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
		super(output, Registries.BLOCK, lookupProvider);
	}

	@Override
	protected abstract void addTags(HolderLookup.Provider provider);
}
