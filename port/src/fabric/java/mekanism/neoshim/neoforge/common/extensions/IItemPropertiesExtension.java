package mekanism.neoshim.neoforge.common.extensions;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;

/** Injected into {@link Item.Properties}. */
public interface IItemPropertiesExtension {

    default <T> Item.Properties component(Supplier<? extends DataComponentType<T>> type, T value) {
        return ((Item.Properties) this).component(type.get(), value);
    }

    /** Implemented by ItemPropertiesMixin (stores the flag read by IItemExtension#isRepairable). */
    default Item.Properties setNoRepair() {
        throw new IllegalStateException("Implemented by mixin");
    }
}
