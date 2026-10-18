package mekanism.neoshim.neoforge.common.crafting;

import java.util.List;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.minecraft.world.item.ItemStack;

/** NeoForge-style custom ingredient, implemented on top of Fabric's {@link CustomIngredient}. */
public interface ICustomIngredient extends CustomIngredient {

    @Override
    boolean test(ItemStack stack);

    Stream<ItemStack> getItems();

    boolean isSimple();

    @Override
    default List<ItemStack> getMatchingStacks() {
        return getItems().toList();
    }

    @Override
    default boolean requiresTesting() {
        return !isSimple();
    }
}
