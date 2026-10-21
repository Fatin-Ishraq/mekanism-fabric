package mekanism.neoshim.neoforge.client.extensions;

import java.util.Map;
import mekanism.neoshim.neoforge.client.model.standalone.StandaloneModelBakingResult;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.impl.client.model.loading.BakedModelsHooks;
import net.minecraft.client.resources.model.ModelBakery;

/** Injected into {@link ModelBakery.BakingResult}: NeoForge adds the standalone models to the baking result. */
public interface BakingResultExtension {

    default StandaloneModelBakingResult standaloneModels() {
        Map<ExtraModelKey<?>, ?> extra = ((BakedModelsHooks) this).fabric_getExtraModels();
        return new StandaloneModelBakingResult(extra == null ? Map.of() : extra);
    }
}
