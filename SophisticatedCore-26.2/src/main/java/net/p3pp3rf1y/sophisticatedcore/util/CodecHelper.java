package net.p3pp3rf1y.sophisticatedcore.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.*;

public class CodecHelper {
	public static final Codec<ItemStack> OVERSIZED_ITEM_STACK_CODEC = Codec
			.lazyInitialized(
					() -> RecordCodecBuilder.create(instance -> instance
							.group(Item.CODEC.fieldOf("id").forGetter(ItemStack::typeHolder),
									Codec.INT.fieldOf("count").orElse(1).forGetter(ItemStack::getCount), DataComponentPatch.CODEC
											.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStack::getComponentsPatch))
							.apply(instance, ItemStack::new)));
	public static final Codec<ItemStack> OPTIONAL_OVERSIZED_ITEM_STACK_CODEC = ExtraCodecs.optionalEmptyMap(OVERSIZED_ITEM_STACK_CODEC)
			.xmap(optional -> optional.orElse(ItemStack.EMPTY), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack)).orElse(ItemStack.EMPTY);

	// Replacement record for the private ItemContainerContents.Slot
	public record ContentsSlot(int index, ItemStackTemplate item) {
		public static final Codec<ContentsSlot> CODEC = RecordCodecBuilder.create(instance -> instance
				.group(Codec.intRange(0, 255).fieldOf("slot").forGetter(ContentsSlot::index),
						ItemStackTemplate.CODEC.fieldOf("item").forGetter(ContentsSlot::item))
				.apply(instance, ContentsSlot::new));
	}

	public static final Codec<ContentsSlot> LENIENT_CONTENTS_SLOT_CODEC = ContentsSlot.CODEC;

	public static Codec<ItemContainerContents> LENIENT_ITEM_CONTAINER_CONTENTS_CODEC = LENIENT_CONTENTS_SLOT_CODEC.sizeLimitedListOf(256)
			.xmap(CodecHelper::fromSlots, CodecHelper::asSlots);

	// String encoded UUID necessary when used as unbounded map key as serialization expects keys to be encoded as strings
	public static final Codec<UUID> STRING_ENCODED_UUID = Codec.STRING.xmap(UUID::fromString, UUID::toString);

	public static final PrimitiveCodec<Integer> STRING_ENCODED_INT = new PrimitiveCodec<>() {
		@Override
		public <T> DataResult<Integer> read(final DynamicOps<T> ops, final T input) {
			return ops.getStringValue(input).map(s -> {
				if (s.startsWith("i")) {
					return Integer.parseInt(s.substring(1));
				} else {
					return Integer.parseInt(s);
				}
			});
		}

		@Override
		public <T> T write(final DynamicOps<T> ops, final Integer value) {
			return ops.createString("i" + value);
		}

		@Override
		public String toString() {
			return "Int";
		}
	};

	private CodecHelper() {
	}

	private static ItemContainerContents fromSlots(List<ContentsSlot> slots) {
		List<ItemStack> stacks = new ArrayList<>();
		for (ContentsSlot slot : slots) {
			while (stacks.size() <= slot.index()) {
				stacks.add(ItemStack.EMPTY);
			}
			stacks.set(slot.index(), slot.item().create());
		}
		return ItemContainerContents.fromItems(stacks);
	}

	private static List<ContentsSlot> asSlots(ItemContainerContents contents) {
		List<ContentsSlot> slots = new ArrayList<>();
		List<ItemStack> stacks = contents.allItemsCopyStream().toList();
		for (int i = 0; i < stacks.size(); i++) {
			ItemStack stack = stacks.get(i);
			if (!stack.isEmpty()) {
				slots.add(new ContentsSlot(i, ItemStackTemplate.fromNonEmptyStack(stack)));
			}
		}
		return slots;
	}

	public static <T> Codec<Set<T>> setOf(Codec<T> elementCodec) {
		return new SetCodec<>(elementCodec);
	}

	public static <T> List<T> toMutable(List<T> list) {
		return new ArrayList<>(list);
	}

	public static NonNullList<ItemStack> toMutableNonnullItemStackList(List<ItemStack> list) {
		return toMutableNonnull(list, ItemStack.EMPTY);
	}

	public static <T> NonNullList<T> toMutableNonnull(List<T> list, T defaultElement) {
		NonNullList<T> nonNullList = NonNullList.withSize(list.size(), defaultElement);
		for (int i = 0; i < list.size(); i++) {
			nonNullList.set(i, list.get(i));
		}
		return nonNullList;
	}

	public static <K, V> Map<K, V> toMutable(Map<K, V> map) {
		return new HashMap<>(map);
	}
}
