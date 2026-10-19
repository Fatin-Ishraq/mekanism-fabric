package mekanism.neoshim.neoforge.fluids.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.stream.Stream;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;

/** Fluids of {@code base} not matched by {@code subtracted}: {@code {"neoforge:ingredient_type": "neoforge:difference", "base": ..., "subtracted": ...}}. */
public final class DifferenceFluidIngredient extends FluidIngredient {

    private static final Codec<FluidIngredient> LAZY_CODEC = Codec.lazyInitialized(() -> FluidIngredient.CODEC);
    public static final MapCodec<DifferenceFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          LAZY_CODEC.fieldOf("base").forGetter(DifferenceFluidIngredient::base),
          LAZY_CODEC.fieldOf("subtracted").forGetter(DifferenceFluidIngredient::subtracted)
    ).apply(builder, DifferenceFluidIngredient::new));
    static final StreamCodec<RegistryFriendlyByteBuf, DifferenceFluidIngredient> STREAM_CODEC = StreamCodec.composite(
          FluidIngredientCodecs.lazyStreamCodec(), DifferenceFluidIngredient::base,
          FluidIngredientCodecs.lazyStreamCodec(), DifferenceFluidIngredient::subtracted,
          DifferenceFluidIngredient::new);

    private final FluidIngredient base;
    private final FluidIngredient subtracted;

    public DifferenceFluidIngredient(FluidIngredient base, FluidIngredient subtracted) {
        this.base = base;
        this.subtracted = subtracted;
    }

    public static FluidIngredient of(FluidIngredient base, FluidIngredient subtracted) {
        return new DifferenceFluidIngredient(base, subtracted);
    }

    @Override
    public Stream<Holder<Fluid>> generateFluids() {
        return base.fluids().stream().filter(fluid -> !subtracted.fluids().contains(fluid));
    }

    @Override
    public boolean test(FluidStack stack) {
        return base.test(stack) && !subtracted.test(stack);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public FluidIngredientType<?> getType() {
        return FluidIngredientCodecs.Types.DIFFERENCE;
    }

    public FluidIngredient base() {
        return base;
    }

    public FluidIngredient subtracted() {
        return subtracted;
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, subtracted);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof DifferenceFluidIngredient other && other.base.equals(base) && other.subtracted.equals(subtracted);
    }
}
