package mekanism.neoshim.neoforge.registries.datamaps;

import mekanism.fabric.datamaps.DataMapLoader;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.Nullable;

/** Injected into {@link Holder} (NeoForge's IWithData). */
public interface IWithData<R> {

    @Nullable
    @SuppressWarnings("unchecked")
    default <T> T getData(DataMapType<R, T> type) {
        return DataMapLoader.getData(type, (Holder<R>) this);
    }
}
