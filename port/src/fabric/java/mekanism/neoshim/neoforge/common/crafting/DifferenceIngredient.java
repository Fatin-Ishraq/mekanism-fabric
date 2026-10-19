package mekanism.neoshim.neoforge.common.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** {@code {"neoforge:ingredient_type": "neoforge:difference", "base": ..., "subtracted": ...}}. */
public record DifferenceIngredient(Ingredient base, Ingredient subtracted) implements ICustomIngredient {

    public static final MapCodec<DifferenceIngredient> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          Ingredient.CODEC.fieldOf("base").forGetter(DifferenceIngredient::base),
          Ingredient.CODEC.fieldOf("subtracted").forGetter(DifferenceIngredient::subtracted)
    ).apply(builder, DifferenceIngredient::new));
    public static final IngredientType<DifferenceIngredient> TYPE = new IngredientType<>(CODEC, StreamCodec.composite(
          Ingredient.CONTENTS_STREAM_CODEC, DifferenceIngredient::base,
          Ingredient.CONTENTS_STREAM_CODEC, DifferenceIngredient::subtracted,
          DifferenceIngredient::new));

    public static Ingredient of(Ingredient base, Ingredient subtracted) {
        return new DifferenceIngredient(base, subtracted).toVanilla();
    }

    @Override
    public Stream<Holder<Item>> items() {
        return base.items().filter(item -> !subtracted.acceptsItem(item));
    }

    @Override
    public boolean test(ItemStack stack) {
        return base.test(stack) && !subtracted.test(stack);
    }

    @Override
    public boolean isSimple() {
        return !base.requiresTesting() && !subtracted.requiresTesting();
    }

    @Override
    public IngredientType<?> getType() {
        return TYPE;
    }
}
