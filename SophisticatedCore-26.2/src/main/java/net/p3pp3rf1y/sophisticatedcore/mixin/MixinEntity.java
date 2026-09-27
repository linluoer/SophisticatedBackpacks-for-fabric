package net.p3pp3rf1y.sophisticatedcore.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.p3pp3rf1y.sophisticatedcore.compat.IPersistentDataEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MixinEntity implements IPersistentDataEntity {
	@Unique
	private CompoundTag sophisticatedcore$persistentData;

	@Inject(method = "saveWithoutId", at = @At("TAIL"))
	private void sophisticatedcore$savePersistentData(ValueOutput output, CallbackInfo ci) {
		if (sophisticatedcore$persistentData != null && !sophisticatedcore$persistentData.isEmpty()) {
			output.store("sophisticatedcore$persistentData", CompoundTag.CODEC, sophisticatedcore$persistentData);
		}
	}

	@Inject(method = "load", at = @At("TAIL"))
	private void sophisticatedcore$loadPersistentData(ValueInput input, CallbackInfo ci) {
		input.read("sophisticatedcore$persistentData", CompoundTag.CODEC).ifPresent(tag -> {
			sophisticatedcore$persistentData = tag;
		});
	}

	@Override
	public CompoundTag sophisticatedcore$getPersistentData() {
		if (sophisticatedcore$persistentData == null) {
			sophisticatedcore$persistentData = new CompoundTag();
		}
		return sophisticatedcore$persistentData;
	}
}
