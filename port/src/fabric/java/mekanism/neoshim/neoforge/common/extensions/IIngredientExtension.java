package mekanism.neoshim.neoforge.common.extensions;

import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

/** Injected into {@link Ingredient}. */
public interface IIngredientExtension {

    private Ingredient self() {
        return (Ingredient) (Object) this;
    }

    default boolean isCustom() {
        return self().getCustomIngredient() != null;
    }

    default boolean isSimple() {
        return !self().requiresTesting();
    }

    default boolean hasNoItems() {
        return self().isEmpty();
    }

    default HolderSet<Item> getValues() {
        return self().values;
    }
}
