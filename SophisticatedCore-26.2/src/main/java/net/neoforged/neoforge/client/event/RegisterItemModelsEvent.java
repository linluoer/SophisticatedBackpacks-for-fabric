package net.neoforged.neoforge.client.event;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.resources.Identifier;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Compatibility shim for {@code RegisterItemModelsEvent}.
 * <p>
 * Fired to allow mods to register custom item model loaders.
 * 桥接到 vanilla 26.2 的 {@code ItemModels.ID_MAPPER}。
 * 使用反射访问，因为 merged jar 中 ID_MAPPER 为 private（由 fabric-transitive-access-wideners-v1 在运行时 widen，
 * 但编译期不一定生效）。
 */
public class RegisterItemModelsEvent extends Event {

	/**
	 * Register a custom item model unbaked loader under the given identifier.
	 *
	 * @param id        the identifier of the item model loader
	 * @param mapCodec  the map codec used to deserialize the custom item model
	 */
	public void register(Identifier id, MapCodec<? extends ItemModel.Unbaked> mapCodec) {
		try {
			Class<?> clazz = net.minecraft.client.renderer.item.ItemModels.class;
			Field field = clazz.getDeclaredField("ID_MAPPER");
			field.setAccessible(true);
			Object mapper = field.get(null);
			Method putMethod = mapper.getClass().getMethod("put", Object.class, Object.class);
			putMethod.invoke(mapper, id, mapCodec);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Failed to register item model " + id, e);
		}
	}
}
