package mekanism.neoshim.neoforge.common.crafting;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** Intersection of ingredients: {@code {"neoforge:ingredient_type": "neoforge:intersection", "children": [...]}}. */
public record IntersectionIngredient(List<Ingredient> children) implements ICustomIngredient {

    public static final MapCodec<IntersectionIngredient> CODEC = Ingredient.CODEC.listOf()
          .validate(list -> list.isEmpty() ? DataResult.error(() -> "Intersection ingredient must have at least one child") : DataResult.success(list))
          .fieldOf("children").xmap(IntersectionIngredient::new, IntersectionIngredient::children);
    public static final IngredientType<IntersectionIngredient> TYPE = new IngredientType<>(CODEC,
          Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).map(IntersectionIngredient::new, IntersectionIngredient::children));

    public IntersectionIngredient {
        if (children.isEmpty()) {
            throw new IllegalArgumentException("Intersection ingredient must have at least one child");
        }
        children = List.copyOf(children);
    }

    public static Ingredient of(Ingredient... children) {
        if (children.length == 1) {
            return children[0];
        }
        return new IntersectionIngredient(List.of(children)).toVanilla();
    }

    @Override
    public boolean test(ItemStack stack) {
        for (Ingredient child : children) {
            if (!child.test(stack)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Stream<Holder<Item>> items() {
        return children.getFirst().items().filter(item -> {
            for (int i = 1; i < children.size(); i++) {
                if (!children.get(i).acceptsItem(item)) {
                    return false;
                }
            }
            return true;
        });
    }

    @Override
    public boolean isSimple() {
        return children.stream().noneMatch(Ingredient::requiresTesting);
    }

    @Override
    public IngredientType<?> getType() {
        return TYPE;
    }
}
