package mekanism.neoshim.neoforge.common.extensions;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Injected into {@link ItemStack}: mutating component overloads taking suppliers. */
public interface IMutableDataComponentHolderExtension {

    private ItemStack self() {
        return (ItemStack) (Object) this;
    }

    @Nullable
    default <T> T set(Supplier<? extends DataComponentType<T>> type, @Nullable T value) {
        return self().set(type.get(), value);
    }

    @Nullable
    default <T, U> T update(Supplier<? extends DataComponentType<T>> type, T defaultValue, U updateContext, BiFunction<T, U, T> updater) {
        return self().update(type.get(), defaultValue, updateContext, updater);
    }

    @Nullable
    default <T> T update(Supplier<? extends DataComponentType<T>> type, T defaultValue, UnaryOperator<T> updater) {
        return self().update(type.get(), defaultValue, updater);
    }

    @Nullable
    default <T> T remove(Supplier<? extends DataComponentType<? extends T>> type) {
        return self().remove(type.get());
    }
}
