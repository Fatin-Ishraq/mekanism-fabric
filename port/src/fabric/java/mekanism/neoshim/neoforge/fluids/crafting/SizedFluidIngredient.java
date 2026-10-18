package mekanism.neoshim.neoforge.fluids.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Objects;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

public final class SizedFluidIngredient {

    public static final Codec<SizedFluidIngredient> FLAT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
          FluidIngredient.MAP_CODEC_NONEMPTY.forGetter(SizedFluidIngredient::ingredient),
          ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(SizedFluidIngredient::amount)
    ).apply(instance, SizedFluidIngredient::new));
    public static final Codec<SizedFluidIngredient> NESTED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
          FluidIngredient.CODEC_NON_EMPTY.fieldOf("ingredient").forGetter(SizedFluidIngredient::ingredient),
          ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(SizedFluidIngredient::amount)
    ).apply(instance, SizedFluidIngredient::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SizedFluidIngredient> STREAM_CODEC = StreamCodec.composite(
          FluidIngredient.STREAM_CODEC, SizedFluidIngredient::ingredient,
          ByteBufCodecs.VAR_INT, SizedFluidIngredient::amount,
          SizedFluidIngredient::new);

    public static SizedFluidIngredient of(FluidStack stack) {
        return new SizedFluidIngredient(FluidIngredient.single(stack), stack.getAmount());
    }

    public static SizedFluidIngredient of(Fluid fluid, int amount) {
        return new SizedFluidIngredient(FluidIngredient.single(fluid), amount);
    }

    public static SizedFluidIngredient of(TagKey<Fluid> tag, int amount) {
        return new SizedFluidIngredient(FluidIngredient.tag(tag), amount);
    }

    private final FluidIngredient ingredient;
    private final int amount;
    @Nullable
    private FluidStack[] cachedStacks;

    public SizedFluidIngredient(FluidIngredient ingredient, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        this.ingredient = ingredient;
        this.amount = amount;
    }

    public FluidIngredient ingredient() {
        return ingredient;
    }

    public int amount() {
        return amount;
    }

    public boolean test(FluidStack stack) {
        return ingredient.test(stack) && stack.getAmount() >= amount;
    }

    public FluidStack[] getFluids() {
        if (cachedStacks == null) {
            cachedStacks = Arrays.stream(ingredient.getStacks()).map(s -> s.copyWithAmount(amount)).toArray(FluidStack[]::new);
        }
        return cachedStacks;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof SizedFluidIngredient other && amount == other.amount && ingredient.equals(other.ingredient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredient, amount);
    }

    @Override
    public String toString() {
        return amount + "x " + ingredient;
    }
}
