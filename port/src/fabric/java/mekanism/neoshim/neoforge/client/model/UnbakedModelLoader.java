package mekanism.neoshim.neoforge.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.resources.model.UnbakedModel;

/** Reads a custom model from JSON. Registered under the id used in the model's {@code loader} field. */
public interface UnbakedModelLoader<T extends UnbakedModel> {

    T read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException;
}
