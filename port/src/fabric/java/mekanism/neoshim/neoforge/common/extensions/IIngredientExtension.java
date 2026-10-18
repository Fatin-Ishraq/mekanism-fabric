package mekanism.neoshim.neoforge.common.extensions;

import net.minecraft.world.item.crafting.Ingredient;

/** Injected into {@link Ingredient}. */
public interface IIngredientExtension {

    private Ingredient self() {
        return (Ingredient) (Object) this;
    }

    default boolean isSimple() {
        return !self().requiresTesting();
    }

    default boolean hasNoItems() {
        return self().getItems().length == 0;
    }

    default Ingredient.Value[] getValues() {
        return self().values;
    }
}
