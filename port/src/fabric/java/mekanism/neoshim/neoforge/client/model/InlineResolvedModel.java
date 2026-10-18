package mekanism.neoshim.neoforge.client.model;

import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import org.jspecify.annotations.Nullable;

/** A model defined inline inside another model's JSON (composite children), resolved against an already-discovered parent. */
public record InlineResolvedModel(UnbakedModel wrapped, @Nullable ResolvedModel parent, ModelDebugName name) implements ResolvedModel {

    @Override
    public String debugName() {
        return name.debugName();
    }
}
