package net.p3pp3rf1y.sophisticatedcore.upgrades;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.Slot;
import net.p3pp3rf1y.sophisticatedcore.common.gui.IServerUpdater;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ContentsFilterLogicContainer extends FilterLogicContainer<ContentsFilterLogic> {
	private static final String DATA_CONTENTS_FILTER_TYPE = "contentsFilterType";

	public ContentsFilterLogicContainer(Supplier<ContentsFilterLogic> filterLogic, IServerUpdater serverUpdater, Consumer<Slot> addSlot) {
		super(filterLogic, serverUpdater, addSlot);
		setFilterSlotsEnabled(getFilterType() != ContentsFilterType.STORAGE);
	}

	public void setFilterType(ContentsFilterType depositFilterType) {
		filterLogic.get().setDepositFilterType(depositFilterType);
		setFilterSlotsEnabled(depositFilterType != ContentsFilterType.STORAGE);
		sendDataToServer(() -> NBTHelper.putEnumConstant(new CompoundTag(), DATA_CONTENTS_FILTER_TYPE, depositFilterType));
	}

	private void setFilterSlotsEnabled(boolean enabled) {
		getFilterSlots().forEach(s -> s.setEnabled(enabled));
	}

	@Override
	public boolean handlePacket(CompoundTag data) {
		if (isDifferentFilterLogicsData(data)) {
			return false;
		}

		data.getString(DATA_CONTENTS_FILTER_TYPE).ifPresent(typeName -> setFilterType(ContentsFilterType.fromName(typeName)));
		return super.handlePacket(data);
	}

	public ContentsFilterType getFilterType() {
		return filterLogic.get().getFilterType();
	}
}
