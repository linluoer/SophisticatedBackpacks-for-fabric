package net.neoforged.neoforge.client.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.model.UnbakedModel;

/**
 * Compatibility shim for {@code UnbakedModelLoader}.
 * <p>
 * Mirrors the NeoForge loader contract: read a JSON object into an
 * {@link UnbakedModel} using the provided {@link Gson}.
 */
public interface UnbakedModelLoader {

	/**
	 * Read an unbaked model from the supplied JSON object.
	 *
	 * @param jsonObject the JSON representation of the model
	 * @param gson       the gson instance to use for nested deserialization
	 * @return the parsed unbaked model
	 */
	UnbakedModel read(JsonObject jsonObject, Gson gson);
}
