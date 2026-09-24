package net.neoforged.neoforge.common.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Item;

import java.util.concurrent.CompletableFuture;

/**
 * Item tags provider shim for Fabric.
 * Extends TagsProvider&lt;Item&gt; with the ITEM registry key.
 */
public abstract class ItemTagsProvider extends TagsProvider<Item> {
	public ItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
		super(output, Registries.ITEM, lookupProvider);
	}

	@Override
	protected abstract void addTags(HolderLookup.Provider provider);
}
