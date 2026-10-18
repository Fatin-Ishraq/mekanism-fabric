package mekanism.neoshim.neoforge.common.extensions;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;

/** Injected into {@link Item.Properties}. */
public interface IItemPropertiesExtension {

    /** Items built from properties marked {@link #setNoCombineRepair()}; filled by ItemNoCombineRepairMixin. */
    Set<Item> NO_COMBINE_REPAIR = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    static boolean noCombineRepair(Item item) {
        return NO_COMBINE_REPAIR.contains(item);
    }

    default <T> Item.Properties component(Supplier<? extends DataComponentType<T>> type, T value) {
        return ((Item.Properties) this).component(type.get(), value);
    }

    /** Implemented by ItemPropertiesMixin. */
    default Item.Properties setNoCombineRepair() {
        throw new IllegalStateException("Implemented by mixin");
    }

    /** Implemented by ItemPropertiesMixin. */
    default boolean mekanism$noCombineRepair() {
        return false;
    }
}
