package mekanism.neoshim.neoforge.client.model.standalone;

import java.util.Map;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import org.jspecify.annotations.Nullable;

/** The baked standalone models of one model reload, backed by Fabric's extra models. */
public final class StandaloneModelBakingResult {

    private final Map<ExtraModelKey<?>, ?> extraModels;

    public StandaloneModelBakingResult(Map<ExtraModelKey<?>, ?> extraModels) {
        this.extraModels = extraModels;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T get(StandaloneModelKey<T> key) {
        return (T) extraModels.get(key.fabricKey());
    }
}
