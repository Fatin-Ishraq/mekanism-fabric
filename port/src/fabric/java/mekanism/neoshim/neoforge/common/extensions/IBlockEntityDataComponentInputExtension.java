package mekanism.neoshim.neoforge.common.extensions;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/** Injected into {@link BlockEntity.DataComponentInput}. */
public interface IBlockEntityDataComponentInputExtension {

    @Nullable
    default <T> T get(Supplier<? extends DataComponentType<T>> type) {
        return ((BlockEntity.DataComponentInput) this).get(type.get());
    }

    default <T> T getOrDefault(Supplier<? extends DataComponentType<T>> type, T defaultValue) {
        return ((BlockEntity.DataComponentInput) this).getOrDefault(type.get(), defaultValue);
    }
}
