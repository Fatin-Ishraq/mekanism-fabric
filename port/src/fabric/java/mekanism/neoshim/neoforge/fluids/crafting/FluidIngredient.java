package mekanism.neoshim.neoforge.fluids.crafting;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import mekanism.neoshim.neoforge.fluids.crafting.display.FluidSlotDisplay;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

/** Fluid equivalent of {@link net.minecraft.world.item.crafting.Ingredient}. Serialization: see {@link FluidIngredientCodecs}. */
public abstract class FluidIngredient implements Predicate<FluidStack> {

    public static final Codec<FluidIngredient> CODEC = FluidIngredientCodecs.CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidIngredient> STREAM_CODEC = FluidIngredientCodecs.STREAM_CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<FluidIngredient>> OPTIONAL_STREAM_CODEC = ByteBufCodecs.optional(STREAM_CODEC);

    @Nullable
    private List<Holder<Fluid>> fluids;

    public final List<Holder<Fluid>> fluids() {
        if (fluids == null) {
            fluids = generateFluids().distinct().toList();
        }
        return fluids;
    }

    @Override
    public abstract boolean test(FluidStack stack);

    protected abstract Stream<Holder<Fluid>> generateFluids();

    public SlotDisplay display() {
        return new SlotDisplay.Composite(fluids().stream().map(FluidIngredient::displayForSingleFluid).toList());
    }

    public abstract boolean isSimple();

    public abstract FluidIngredientType<?> getType();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    public static SlotDisplay displayForSingleFluid(Holder<Fluid> fluid) {
        return new FluidSlotDisplay(fluid);
    }

    public static FluidIngredient of(FluidStack... fluids) {
        return of(Arrays.stream(fluids).map(FluidStack::getFluid));
    }

    public static FluidIngredient of(Fluid... fluids) {
        return of(Arrays.stream(fluids));
    }

    @SuppressWarnings("deprecation")
    public static FluidIngredient of(Stream<Fluid> fluids) {
        return of(HolderSet.direct(fluids.map(Fluid::builtInRegistryHolder).toList()));
    }

    public static FluidIngredient of(HolderSet<Fluid> fluids) {
        return new SimpleFluidIngredient(fluids);
    }
}
