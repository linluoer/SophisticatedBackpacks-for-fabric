package net.neoforged.neoforge.client.event;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.minecraft.resources.Identifier;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * Compatibility shim for {@code RegisterBlockStateModels}.
 * <p>
 * Fired to allow mods to register custom block-state model loaders.
 * 桥接到 Fabric API 的 {@link CustomUnbakedBlockStateModel#register}。
 */
public class RegisterBlockStateModels extends Event {
	/**
	 * Register a custom unbaked block-state model under the given identifier.
	 *
	 * @param id    the identifier of the model loader
	 * @param codec the map codec used to deserialize the custom block-state model
	 */
	public void registerModel(Identifier id, MapCodec<? extends CustomUnbakedBlockStateModel> codec) {
		CustomUnbakedBlockStateModel.register(id, codec);
	}
}
