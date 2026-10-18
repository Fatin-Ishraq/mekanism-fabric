package mekanism.neoshim.neoforge.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Objects;
import mekanism.fabric.compat.IngredientCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

/** Ingredient with a count. FLAT_CODEC format matches NeoForge ({@code {"count": n, <ingredient fields>}}). */
public final class SizedIngredient {

    public static final Codec<SizedIngredient> FLAT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
          IngredientCodecs.MAP_CODEC_NONEMPTY.forGetter(SizedIngredient::ingredient),
          ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(SizedIngredient::count)
    ).apply(instance, SizedIngredient::new));
    public static final Codec<SizedIngredient> NESTED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
          Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(SizedIngredient::ingredient),
          ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(SizedIngredient::count)
    ).apply(instance, SizedIngredient::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SizedIngredient> STREAM_CODEC = StreamCodec.composite(
          Ingredient.CONTENTS_STREAM_CODEC, SizedIngredient::ingredient,
          ByteBufCodecs.VAR_INT, SizedIngredient::count,
          SizedIngredient::new);

    private final Ingredient ingredient;
    private final int count;
    @Nullable
    private ItemStack[] cachedStacks;

    public SizedIngredient(Ingredient ingredient, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        this.ingredient = ingredient;
        this.count = count;
    }

    public static SizedIngredient of(ItemLike item, int count) {
        return new SizedIngredient(Ingredient.of(item), count);
    }

    public static SizedIngredient of(TagKey<Item> tag, int count) {
        return new SizedIngredient(Ingredient.of(tag), count);
    }

    public Ingredient ingredient() {
        return ingredient;
    }

    public int count() {
        return count;
    }

    public boolean test(ItemStack stack) {
        return ingredient.test(stack) && stack.getCount() >= count;
    }

    public ItemStack[] getItems() {
        if (cachedStacks == null) {
            cachedStacks = Arrays.stream(ingredient.getItems()).map(s -> s.copyWithCount(count)).toArray(ItemStack[]::new);
        }
        return cachedStacks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof SizedIngredient other && count == other.count && ingredient.equals(other.ingredient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredient, count);
    }

    @Override
    public String toString() {
        return count + "x " + ingredient;
    }
}
