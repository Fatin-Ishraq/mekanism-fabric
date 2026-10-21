package mekanism.neoshim.neoforge.fluids.crafting;

import java.util.Arrays;
import java.util.List;
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

/**
 * Fluid equivalent of an ingredient. Fabric has no fluid ingredients, so this is a self-contained implementation using NeoForge's JSON format:
 * a holder-set form ({@code "#tag"} or a list of fluid ids) or {@code {"neoforge:ingredient_type": "neoforge:<type>", ...}}.
 */
public abstract class FluidIngredient implements Predicate<FluidStack> {

    public static final net.minecraft.network.codec.StreamCodec<RegistryFriendlyByteBuf, java.util.Optional<FluidIngredient>> OPTIONAL_STREAM_CODEC =
          ByteBufCodecs.optional(FluidIngredientTypes.STREAM_CODEC);
    public static final com.mojang.serialization.Codec<FluidIngredient> CODEC = FluidIngredientTypes.CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidIngredient> STREAM_CODEC = FluidIngredientTypes.STREAM_CODEC;

    @Nullable
    private List<Holder<Fluid>> fluids;

    public final List<Holder<Fluid>> fluids() {
        if (fluids == null) {
            fluids = generateFluids().toList();
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

    public static FluidIngredient of(Stream<Fluid> fluids) {
        return of(HolderSet.direct(fluids.map(Fluid::builtInRegistryHolder).toList()));
    }

    public static FluidIngredient of(HolderSet<Fluid> set) {
        return new SimpleFluidIngredient(set);
    }
}
