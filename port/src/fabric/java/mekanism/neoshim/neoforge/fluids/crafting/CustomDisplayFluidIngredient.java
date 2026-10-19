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
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.material.Fluid;

/** Wraps a fluid ingredient with an explicit display. */
public final class CustomDisplayFluidIngredient extends FluidIngredient {

    public static final MapCodec<CustomDisplayFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          Codec.lazyInitialized(() -> FluidIngredient.CODEC).fieldOf("base").forGetter(CustomDisplayFluidIngredient::base),
          SlotDisplay.CODEC.fieldOf("display").forGetter(CustomDisplayFluidIngredient::display)
    ).apply(builder, CustomDisplayFluidIngredient::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, CustomDisplayFluidIngredient> STREAM_CODEC = StreamCodec.composite(
          FluidIngredientCodecs.lazyStreamCodec(), CustomDisplayFluidIngredient::base,
          SlotDisplay.STREAM_CODEC, CustomDisplayFluidIngredient::display,
          CustomDisplayFluidIngredient::new);

    private final FluidIngredient base;
    private final SlotDisplay display;

    public CustomDisplayFluidIngredient(FluidIngredient base, SlotDisplay display) {
        this.base = base;
        this.display = display;
    }

    public static FluidIngredient of(FluidIngredient base, SlotDisplay display) {
        return new CustomDisplayFluidIngredient(base, display);
    }

    @Override
    public boolean test(FluidStack stack) {
        return base.test(stack);
    }

    @Override
    public Stream<Holder<Fluid>> generateFluids() {
        return base.fluids().stream();
    }

    @Override
    public boolean isSimple() {
        return base.isSimple();
    }

    @Override
    public FluidIngredientType<?> getType() {
        return FluidIngredientCodecs.Types.CUSTOM_DISPLAY;
    }

    public FluidIngredient base() {
        return base;
    }

    @Override
    public SlotDisplay display() {
        return display;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof CustomDisplayFluidIngredient other && other.base.equals(base) && other.display.equals(display);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, display);
    }

    @Override
    public String toString() {
        return "CustomDisplayFluidIngredient[" + base + ", " + display + "]";
    }
}
