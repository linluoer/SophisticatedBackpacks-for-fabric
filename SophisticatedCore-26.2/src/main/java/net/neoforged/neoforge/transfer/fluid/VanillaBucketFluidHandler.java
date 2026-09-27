package net.neoforged.neoforge.transfer.fluid;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.Optional;

/**
 * 为 vanilla 桶类物品（水桶、岩浆桶、空桶等）提供 ResourceHandler&lt;FluidResource&gt; 适配。
 * <p>
 * 桶的容量固定为 1000mB（1 桶）。空桶可接受任何有对应桶物品的流体；
 * 满桶可抽取其中的流体，抽取后替换为空桶。
 */
public class VanillaBucketFluidHandler implements ResourceHandler<FluidResource> {
	private static final int BUCKET_CAPACITY = 1000;

	private final ItemAccess itemAccess;
	private final ItemStack initialStack;

	public VanillaBucketFluidHandler(ItemAccess itemAccess) {
		this.itemAccess = itemAccess;
		this.initialStack = itemAccess.getStack().copy();
	}

	public static Optional<ResourceHandler<FluidResource>> of(ItemAccess itemAccess) {
		ItemStack stack = itemAccess.getStack();
		if (stack.isEmpty()) {
			return Optional.empty();
		}
		Item item = stack.getItem();
		if (item == Items.BUCKET || item instanceof BucketItem) {
			return Optional.of(new VanillaBucketFluidHandler(itemAccess));
		}
		return Optional.empty();
	}

	private boolean isEmptyBucket() {
		return initialStack.getItem() == Items.BUCKET;
	}

	private Fluid getFluid() {
		if (initialStack.getItem() instanceof BucketItem bucketItem) {
			return bucketItem.getContent();
		}
		return Fluids.EMPTY;
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public FluidResource getResource(int index) {
		// 基于当前 itemAccess 中的物品状态，而不是 initialStack 缓存
		ItemStack currentStack = itemAccess.getStack();
		Item currentItem = currentStack.getItem();
		if (currentItem == Items.BUCKET) {
			return FluidResource.EMPTY;
		}
		if (currentItem instanceof BucketItem bucketItem) {
			return FluidResource.of(bucketItem.getContent());
		}
		return FluidResource.EMPTY;
	}

	@Override
	public long getAmountAsLong(int index) {
		// 基于当前 itemAccess 中的物品状态
		ItemStack currentStack = itemAccess.getStack();
		Item currentItem = currentStack.getItem();
		if (currentItem == Items.BUCKET) {
			return 0;
		}
		if (currentItem instanceof BucketItem) {
			return BUCKET_CAPACITY;
		}
		return 0;
	}

	@Override
	public long getCapacityAsLong(int index, FluidResource resource) {
		return BUCKET_CAPACITY;
	}

	@Override
	public boolean isValid(int index, FluidResource resource) {
		if (isEmptyBucket()) {
			return !resource.isEmpty() && resource.getFluid().getBucket() != Items.AIR;
		}
		return false;
	}

	@Override
	public int insert(int index, FluidResource resource, int amount, TransactionContext tx) {
		if (!isEmptyBucket() || resource.isEmpty() || amount < BUCKET_CAPACITY) {
			return 0;
		}
		Item bucketItem = resource.getFluid().getBucket();
		if (bucketItem == null || bucketItem == Items.AIR) {
			return 0;
		}
		itemAccess.exchange(ItemResource.of(new ItemStack(bucketItem)), 1, tx);
		return BUCKET_CAPACITY;
	}

	@Override
	public int extract(int index, FluidResource resource, int amount, TransactionContext tx) {
		if (isEmptyBucket() || resource.isEmpty() || amount < BUCKET_CAPACITY) {
			return 0;
		}
		if (resource.getFluid() != getFluid()) {
			return 0;
		}
		itemAccess.exchange(ItemResource.of(new ItemStack(Items.BUCKET)), 1, tx);
		return BUCKET_CAPACITY;
	}
}
