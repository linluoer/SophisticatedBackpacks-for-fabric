package net.p3pp3rf1y.sophisticatedcore.data;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.util.Util;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;

import java.util.concurrent.CompletableFuture;

public class DataGenerators {
	private DataGenerators() {
	}

	public static void gatherData(GatherDataEvent.Client evt) {
		CompletableFuture<HolderLookup.Provider> registries = CompletableFuture.supplyAsync(VanillaRegistries::createLookup, Util.backgroundExecutor());
		evt.createProvider(output -> new CoreFluidTagsProvider(output, registries));
		evt.createProvider(output -> new CoreRecipeProvider.Runner(output, registries));
		evt.createProvider(CoreModelProvider::new);
	}

	private static class CoreModelProvider extends SophisticatedModelProvider {
		public CoreModelProvider(PackOutput output) {
			super(output, SophisticatedCore.MOD_ID);
		}

		protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
			// DynamicFluidContainerModel 不存在于 Fabric，跳过 XP bucket 模型生成
		}
	}
}
