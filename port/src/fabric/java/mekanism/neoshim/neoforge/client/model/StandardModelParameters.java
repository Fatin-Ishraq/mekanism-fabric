package mekanism.neoshim.neoforge.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.math.Transformation;
import java.util.Map;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import org.jspecify.annotations.Nullable;

/** The vanilla and NeoForge model properties every custom model JSON may carry. */
public record StandardModelParameters(@Nullable Identifier parent, TextureSlots.Data textures, @Nullable ItemTransforms itemTransforms,
                                      @Nullable Boolean ambientOcclusion, UnbakedModel.@Nullable GuiLight guiLight, Transformation rootTransform,
                                      Map<String, Boolean> partVisibility) {

    public static StandardModelParameters parse(JsonObject json, JsonDeserializationContext context) {
        String parentName = GsonHelper.getAsString(json, "parent", "");
        Identifier parent = parentName.isEmpty() ? null : Identifier.parse(parentName);
        TextureSlots.Data textures = json.has("textures") ? TextureSlots.parseTextureMap(GsonHelper.getAsJsonObject(json, "textures")) : TextureSlots.Data.EMPTY;
        ItemTransforms itemTransforms = json.has("display") ? context.deserialize(GsonHelper.getAsJsonObject(json, "display"), ItemTransforms.class) : null;
        Boolean ambientOcclusion = json.has("ambientocclusion") ? GsonHelper.getAsBoolean(json, "ambientocclusion") : null;
        UnbakedModel.GuiLight guiLight = json.has("gui_light") ? UnbakedModel.GuiLight.getByName(GsonHelper.getAsString(json, "gui_light")) : null;
        Transformation rootTransform = NeoForgeModelProperties.deserializeRootTransform(json, context);
        Map<String, Boolean> partVisibility = NeoForgeModelProperties.deserializePartVisibility(json);
        return new StandardModelParameters(parent, textures, itemTransforms, ambientOcclusion, guiLight, rootTransform, partVisibility);
    }
}
