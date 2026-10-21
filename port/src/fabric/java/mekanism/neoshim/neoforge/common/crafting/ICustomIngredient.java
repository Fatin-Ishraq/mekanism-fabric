package mekanism.neoshim.neoforge.common.crafting;

import java.util.stream.Stream;
import mekanism.fabric.compat.IngredientCodecs;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** NeoForge-style custom ingredient, implemented on top of Fabric's {@link CustomIngredient}. */
public interface ICustomIngredient extends CustomIngredient {

    @Override
    boolean test(ItemStack stack);

    @Override
    Stream<Holder<Item>> items();

    boolean isSimple();

    IngredientType<?> getType();

    @Override
    default boolean requiresTesting() {
        return !isSimple();
    }

    @Override
    default CustomIngredientSerializer<?> getSerializer() {
        return IngredientCodecs.serializer(getType());
    }
}
