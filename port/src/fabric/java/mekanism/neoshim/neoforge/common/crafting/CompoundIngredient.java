package mekanism.neoshim.neoforge.common.crafting;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;

/** Union of ingredients: {@code {"neoforge:ingredient_type": "neoforge:compound", "children": [...]}}. */
public record CompoundIngredient(List<Ingredient> children) implements ICustomIngredient {

    public static final MapCodec<CompoundIngredient> CODEC = Ingredient.CODEC.listOf()
          .validate(list -> list.isEmpty() ? DataResult.error(() -> "Compound ingredient must have at least one child") : DataResult.success(list))
          .fieldOf("children").xmap(CompoundIngredient::new, CompoundIngredient::children);
    public static final IngredientType<CompoundIngredient> TYPE = new IngredientType<>(CODEC,
          Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).map(CompoundIngredient::new, CompoundIngredient::children));

    public CompoundIngredient {
        if (children.isEmpty()) {
            throw new IllegalArgumentException("Compound ingredient must have at least one child");
        }
        children = List.copyOf(children);
    }

    public static Ingredient of(Ingredient... children) {
        if (children.length == 1) {
            return children[0];
        }
        return new CompoundIngredient(Arrays.asList(children)).toVanilla();
    }

    @Override
    public Stream<Holder<Item>> items() {
        return children.stream().flatMap(Ingredient::items).distinct();
    }

    @Override
    public boolean test(ItemStack stack) {
        for (Ingredient child : children) {
            if (child.test(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSimple() {
        for (Ingredient child : children) {
            if (child.requiresTesting()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public IngredientType<?> getType() {
        return TYPE;
    }

    @Override
    public SlotDisplay display() {
        return new SlotDisplay.Composite(children.stream().map(Ingredient::display).toList());
    }
}
