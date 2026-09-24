package net.neoforged.neoforge.data.event;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * 数据生成事件 - 在数据生成过程中触发。
 */
public class GatherDataEvent extends Event {
	private final DataGenerator generator;
	private final PackOutput packOutput;
	private final boolean includeClient;
	private final boolean includeServer;
	private final boolean includeDev;
	private final boolean includeReports;

	public GatherDataEvent(DataGenerator generator, PackOutput packOutput, boolean includeClient, boolean includeServer, boolean includeDev, boolean includeReports) {
		this.generator = generator;
		this.packOutput = packOutput;
		this.includeClient = includeClient;
		this.includeServer = includeServer;
		this.includeDev = includeDev;
		this.includeReports = includeReports;
	}

	public DataGenerator getGenerator() {
		return generator;
	}

	public PackOutput getPackOutput() {
		return packOutput;
	}

	public boolean includeClient() {
		return includeClient;
	}

	public boolean includeServer() {
		return includeServer;
	}

	public boolean includeDev() {
		return includeDev;
	}

	public boolean includeReports() {
		return includeReports;
	}

	/**
	 * 客户端数据生成事件 - 提供createProvider方法用于注册数据提供器。
	 */
	public static class Client extends GatherDataEvent {
		private final CompletableFuture<HolderLookup.Provider> lookupProvider;

		public Client(DataGenerator generator, PackOutput packOutput, boolean includeClient, boolean includeServer, boolean includeDev, boolean includeReports) {
			super(generator, packOutput, includeClient, includeServer, includeDev, includeReports);
			this.lookupProvider = CompletableFuture.completedFuture(HolderLookup.Provider.create(java.util.stream.Stream.empty()));
		}

		public Client(DataGenerator generator, PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, boolean includeClient, boolean includeServer, boolean includeDev, boolean includeReports) {
			super(generator, packOutput, includeClient, includeServer, includeDev, includeReports);
			this.lookupProvider = lookupProvider;
		}

		public CompletableFuture<HolderLookup.Provider> getLookupProvider() {
			return lookupProvider;
		}

		public <T extends DataProvider> T createProvider(DataProvider.Factory<T> factory) {
			return factory.create(getPackOutput());
		}

		public <T extends DataProvider> T createProvider(BiFunction<PackOutput, CompletableFuture<HolderLookup.Provider>, T> factory) {
			return factory.apply(getPackOutput(), getLookupProvider());
		}

		public <B extends DataProvider, I extends DataProvider> void createBlockAndItemTags(
				BiFunction<PackOutput, CompletableFuture<HolderLookup.Provider>, B> blockProviderFactory,
				ItemTagProviderFactory<B, I> itemProviderFactory) {
			B blockProvider = blockProviderFactory.apply(getPackOutput(), getLookupProvider());
			I itemProvider = itemProviderFactory.apply(getPackOutput(), getLookupProvider(), blockProvider);
		}
	}

	@FunctionalInterface
	public interface ItemTagProviderFactory<B extends DataProvider, I extends DataProvider> {
		I apply(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, B blockTagProvider);
	}
}
