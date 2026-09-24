package net.p3pp3rf1y.sophisticatedcore.network;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.p3pp3rf1y.sophisticatedcore.compat.ScNeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.util.RecipeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-to-client payload that syncs the full collection of {@link RecipeHolder}s in batches.
 * <p>
 * Vanilla MC 26.2 only syncs recipe <em>displays</em> ({@code ClientRecipeContainer}) to the client,
 * not the full {@code RecipeHolder}s. Recipe viewers (JEI/REI) and the compacting/uncompacting logic in
 * {@link RecipeHelper} need access to the full recipe holders on the client, so the mod syncs them itself.
 * <p>
 * 使用 NBT + {@link Recipe#CODEC} 来序列化配方，而不是 {@link RecipeHolder#STREAM_CODEC}。
 * 因为 {@code Recipe.STREAM_CODEC} 使用 {@code ByteBufCodecs.registry(Registries.RECIPE_SERIALIZER)}，
 * 该方式依赖 {@link RegistryFriendlyByteBuf#registryAccess()} 来获取注册表，在 Fabric 网络层
 * 反序列化时可能因注册表不可用导致 {@code DecoderException}。而 {@link Recipe#CODEC} 使用
 * {@code BuiltInRegistries.RECIPE_SERIALIZER.byNameCodec()}，直接访问内置注册表，不依赖 buf。
 * <p>
 * 配方分批发送，避免单个网络包超出大小限制。
 */
public record SyncRecipesPayload(int batchIndex, int totalBatches, List<RecipeHolder<?>> recipes) implements CustomPacketPayload {
	private static final Logger LOGGER = LoggerFactory.getLogger("SyncRecipesPayload");

	public static final Type<SyncRecipesPayload> TYPE = new Type<>(SophisticatedCore.getIdentifier("sync_recipes"));

	/**
	 * 配方列表的自定义 StreamCodec，使用 NBT + Codec 绕过 Recipe.STREAM_CODEC 的注册表依赖问题。
	 * 必须在 STREAM_CODEC 之前定义，避免非法前向引用。
	 */
	private static final StreamCodec<RegistryFriendlyByteBuf, List<RecipeHolder<?>>> RECIPE_LIST_STREAM_CODEC = new StreamCodec<>() {
		@Override
		public void encode(RegistryFriendlyByteBuf buf, List<RecipeHolder<?>> value) {
			buf.writeVarInt(value.size());
			HolderLookup.Provider provider = buf.registryAccess();
			for (RecipeHolder<?> holder : value) {
				// 写入配方 ID（MC 26.2 中 ResourceKey 使用 identifier() 而非 location()）
				buf.writeIdentifier(holder.id().identifier());
				// 用 Recipe.CODEC 将配方编码为 NBT
				var nbtResult = Recipe.CODEC.encodeStart(
						provider.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),
						holder.value());
				var nbt = nbtResult.result().orElse(null);
				if (nbt == null) {
					LOGGER.warn("Failed to encode recipe {}, skipping", holder.id());
					buf.writeNbt(null);
					continue;
				}
				buf.writeNbt((net.minecraft.nbt.Tag) nbt);
			}
		}

		@Override
		public List<RecipeHolder<?>> decode(RegistryFriendlyByteBuf buf) {
			int size = buf.readVarInt();
			List<RecipeHolder<?>> list = new ArrayList<>(size);
			HolderLookup.Provider provider = buf.registryAccess();
			for (int i = 0; i < size; i++) {
				try {
					Identifier id = buf.readIdentifier();
					net.minecraft.nbt.Tag nbt = buf.readNbt();
					if (nbt == null) {
						continue;
					}
					Recipe<?> recipe = Recipe.CODEC.parse(
							provider.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),
							nbt
					).getOrThrow(error -> new RuntimeException("Failed to decode recipe " + id + ": " + error));
					ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, id);
					list.add(new RecipeHolder<>(key, recipe));
				} catch (Exception e) {
					LOGGER.error("Error decoding recipe at index {} in batch", i, e);
					throw e;
				}
			}
			return list;
		}
	};

	/**
	 * 自定义 StreamCodec：使用 NBT + Recipe.CODEC 来序列化配方列表。
	 * <p>
	 * 编码流程：对每个 RecipeHolder，写入 id（Identifier）和 value（Recipe.CODEC -> NBT）
	 * 解码流程：读取 id 和 NBT，用 Recipe.CODEC 解码
	 */
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncRecipesPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, SyncRecipesPayload::batchIndex,
			ByteBufCodecs.VAR_INT, SyncRecipesPayload::totalBatches,
			RECIPE_LIST_STREAM_CODEC, SyncRecipesPayload::recipes,
			SyncRecipesPayload::new);

	/**
	 * 每批发送的配方数量。设置为 200 以确保每批数据远低于 8MB 包大小限制。
	 * 1653 个配方分 9 批发送。
	 */
	public static final int BATCH_SIZE = 200;

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	/**
	 * 客户端接收缓冲区：按玩家 UUID 收集各批次的配方，收到最后一批后重建 RecipeMap。
	 * 使用 ConcurrentHashMap 保证线程安全（payload 可能在网络线程上处理）。
	 */
	private static final Map<java.util.UUID, List<List<RecipeHolder<?>>>> BATCH_BUFFER = new ConcurrentHashMap<>();

	public static void handlePayload(SyncRecipesPayload payload, IPayloadContext context) {
		Player player = context.player();
		if (player == null) {
			return;
		}
		java.util.UUID uuid = player.getUUID();
		List<List<RecipeHolder<?>>> batches = BATCH_BUFFER.computeIfAbsent(uuid, k -> new ArrayList<>());
		synchronized (batches) {
			// 确保批次索引在正确位置
			while (batches.size() <= payload.batchIndex()) {
				batches.add(null);
			}
			batches.set(payload.batchIndex(), payload.recipes());
		}

		// 设置客户端 level，使 RecipeHelper.getLevel() 在部分加载时也能工作
		RecipeHelper.setLevel(player.level());

		if (payload.batchIndex() + 1 < payload.totalBatches()) {
			// 还有更多批次要接收
			return;
		}

		// 收到最后一批，合并所有配方并重建 RecipeMap
		List<RecipeHolder<?>> allRecipes = new ArrayList<>();
		synchronized (batches) {
			for (List<RecipeHolder<?>> batch : batches) {
				if (batch != null) {
					allRecipes.addAll(batch);
				}
			}
			BATCH_BUFFER.remove(uuid);
		}

		RecipeMap recipeMap = RecipeMap.create(allRecipes);
		RecipeHelper.setRecipes(recipeMap);
		// Fire RecipesReceivedEvent to invalidate cached compacting/uncompacting results,
		// mirroring the NeoForge client recipe-received flow.
		ScNeoForge.EVENT_BUS.post(new RecipesReceivedEvent(recipeMap));
		LOGGER.info("SyncRecipesPayload: received all {} batches, {} recipes total, RecipeMap rebuilt",
				payload.totalBatches(), allRecipes.size());
	}
}
