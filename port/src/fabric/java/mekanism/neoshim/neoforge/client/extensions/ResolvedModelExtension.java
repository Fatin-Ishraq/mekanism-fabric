package mekanism.neoshim.neoforge.client.extensions;

import java.util.ArrayList;
import java.util.List;
import mekanism.neoshim.neoforge.client.model.NeoForgeModelProperties;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.util.context.ContextMap;

/** Injected into {@link ResolvedModel}. */
public interface ResolvedModelExtension {

    static ContextMap findTopAdditionalProperties(ResolvedModel topModel) {
        List<ResolvedModel> chain = new ArrayList<>();
        for (ResolvedModel model = topModel; model != null; model = model.parent()) {
            chain.add(model);
        }
        ContextMap.Builder builder = new ContextMap.Builder();
        //Parents first so that children override them
        for (int i = chain.size() - 1; i >= 0; i--) {
            chain.get(i).wrapped().fillAdditionalProperties(builder);
        }
        return NeoForgeModelProperties.create(builder);
    }

    default ContextMap getTopAdditionalProperties() {
        return findTopAdditionalProperties((ResolvedModel) this);
    }
}
