package mekanism.neoshim.neoforge.common.extensions;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelReader;

/** Injected into {@link LevelReader}. */
public interface ILevelReaderExtension {

    default <T> Optional<Holder.Reference<T>> holder(ResourceKey<T> key) {
        return ((LevelReader) this).registryAccess().holder(key);
    }
}
