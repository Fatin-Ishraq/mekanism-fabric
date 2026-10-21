package mekanism.neoshim.neoforge.fluids.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import mekanism.neoshim.neoforge.fluids.crafting.display.FluidTagSlotDisplay;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/** A fluid ingredient backed directly by a holder set: {@code "#tag"} or a list of fluid ids. */
public class SimpleFluidIngredient extends FluidIngredient {

    private static final Codec<HolderSet<Fluid>> HOLDER_SET_NO_EMPTY_FLUID = HolderSetCodec.create(Registries.FLUID,
          BuiltInRegistries.FLUID.holderByNameCodec(), false);
    static final Codec<SimpleFluidIngredient> CODEC = ExtraCodecs.nonEmptyHolderSet(HOLDER_SET_NO_EMPTY_FLUID)
          .xmap(SimpleFluidIngredient::new, SimpleFluidIngredient::fluidSet);
    static final StreamCodec<RegistryFriendlyByteBuf, SimpleFluidIngredient> CONTENTS_STREAM_CODEC = ByteBufCodecs.holderSet(Registries.FLUID)
          .map(SimpleFluidIngredient::new, SimpleFluidIngredient::fluidSet);
    public static final FluidIngredientType<SimpleFluidIngredient> TYPE = FluidIngredientTypes.register(
          Identifier.fromNamespaceAndPath("neoforge", "simple"), MapCodec.assumeMapUnsafe(CODEC), CONTENTS_STREAM_CODEC);

    private final HolderSet<Fluid> values;

    public SimpleFluidIngredient(HolderSet<Fluid> values) {
        values.unwrap().ifRight(list -> {
            if (list.isEmpty()) {
                throw new IllegalStateException("Fluid ingredients can't be empty!");
            }
            for (Holder<Fluid> holder : list) {
                if (holder.is(Fluids.EMPTY.builtInRegistryHolder())) {
                    throw new IllegalStateException("Fluid ingredients can't contain the empty fluid");
                }
            }
        });
        this.values = values;
    }

    @Override
    public boolean test(FluidStack stack) {
        return stack.is(values);
    }

    @Override
    protected Stream<Holder<Fluid>> generateFluids() {
        return values.stream();
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public FluidIngredientType<?> getType() {
        return TYPE;
    }

    @Override
    public SlotDisplay display() {
        return values.unwrapKey().<SlotDisplay>map(FluidTagSlotDisplay::new).orElseGet(super::display);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof SimpleFluidIngredient other && other.values.equals(this.values);
    }

    public HolderSet<Fluid> fluidSet() {
        return values;
    }
}
