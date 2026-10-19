package mekanism.neoshim.neoforge.common.extensions;

import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Injected into {@link Ingredient}: the methods NeoForge 26.1 patches into the vanilla class. {@code getCustomIngredient()} comes from Fabric's
 * {@code FabricIngredient} (the shim's custom ingredients implement Fabric's {@code CustomIngredient}).
 */
public interface IIngredientExtension {

    private Ingredient self() {
        return (Ingredient) (Object) this;
    }

    /** Whether this wraps a custom ingredient (Fabric's custom ingredients have placeholder values). */
    default boolean isCustom() {
        return self().getCustomIngredient() != null;
    }

    /** Whether matching only depends on the item, so {@link #getValues()} can be used directly. */
    default boolean isSimple() {
        return !isCustom();
    }

    default HolderSet<Item> getValues() {
        return self().values;
    }
}
