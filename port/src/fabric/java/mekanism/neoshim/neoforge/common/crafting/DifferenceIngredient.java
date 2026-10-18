package mekanism.neoshim.neoforge.common.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.stream.Stream;
import mekanism.fabric.compat.IngredientCodecs;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** Serialized like NeoForge: {@code {"type": "neoforge:difference", "base": ..., "subtracted": ...}}. */
public record DifferenceIngredient(Ingredient base, Ingredient subtracted) implements ICustomIngredient {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("neoforge", "difference");
    public static final MapCodec<DifferenceIngredient> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          Ingredient.CODEC_NONEMPTY.fieldOf("base").forGetter(DifferenceIngredient::base),
          Ingredient.CODEC_NONEMPTY.fieldOf("subtracted").forGetter(DifferenceIngredient::subtracted)
    ).apply(builder, DifferenceIngredient::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, DifferenceIngredient> STREAM_CODEC = StreamCodec.composite(
          Ingredient.CONTENTS_STREAM_CODEC, DifferenceIngredient::base,
          Ingredient.CONTENTS_STREAM_CODEC, DifferenceIngredient::subtracted,
          DifferenceIngredient::new);
    public static final CustomIngredientSerializer<DifferenceIngredient> SERIALIZER = IngredientCodecs.serializer(ID, CODEC, STREAM_CODEC);

    public static Ingredient of(Ingredient base, Ingredient subtracted) {
        return new DifferenceIngredient(base, subtracted).toVanilla();
    }

    @Override
    public Stream<ItemStack> getItems() {
        return Arrays.stream(base.getItems()).filter(stack -> !subtracted.test(stack));
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
    public CustomIngredientSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
