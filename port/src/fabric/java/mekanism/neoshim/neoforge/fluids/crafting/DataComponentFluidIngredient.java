package mekanism.neoshim.neoforge.fluids.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.stream.Stream;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import mekanism.neoshim.neoforge.fluids.FluidType;
import mekanism.neoshim.neoforge.fluids.crafting.display.FluidStackSlotDisplay;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.material.Fluid;

public final class DataComponentFluidIngredient extends FluidIngredient {

    public static final MapCodec<DataComponentFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          HolderSetCodec.create(Registries.FLUID, BuiltInRegistries.FLUID.holderByNameCodec(), false).fieldOf("fluids").forGetter(DataComponentFluidIngredient::fluidSet),
          DataComponentExactPredicate.CODEC.fieldOf("components").forGetter(DataComponentFluidIngredient::components),
          Codec.BOOL.optionalFieldOf("strict", false).forGetter(DataComponentFluidIngredient::isStrict)
    ).apply(builder, DataComponentFluidIngredient::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, DataComponentFluidIngredient> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.holderSet(Registries.FLUID), DataComponentFluidIngredient::fluidSet,
          DataComponentExactPredicate.STREAM_CODEC, DataComponentFluidIngredient::components,
          ByteBufCodecs.BOOL, DataComponentFluidIngredient::isStrict,
          DataComponentFluidIngredient::new);
    public static final FluidIngredientType<DataComponentFluidIngredient> TYPE = FluidIngredientTypes.register(
          Identifier.fromNamespaceAndPath("neoforge", "components"), CODEC, STREAM_CODEC);

    private final HolderSet<Fluid> fluids;
    private final DataComponentExactPredicate components;
    private final boolean strict;
    private final FluidStack[] stacks;

    public DataComponentFluidIngredient(HolderSet<Fluid> fluids, DataComponentExactPredicate components, boolean strict) {
        this.fluids = fluids;
        this.components = components;
        this.strict = strict;
        this.stacks = fluids.stream().map(fluid -> new FluidStack(fluid, FluidType.BUCKET_VOLUME, components.asPatch())).toArray(FluidStack[]::new);
    }

    @Override
    public boolean test(FluidStack stack) {
        if (strict) {
            for (FluidStack stack2 : this.stacks) {
                if (FluidStack.isSameFluidSameComponents(stack, stack2)) {
                    return true;
                }
            }
            return false;
        }
        return this.fluids.contains(stack.getFluidHolder()) && this.components.test(stack);
    }

    @Override
    protected Stream<Holder<Fluid>> generateFluids() {
        return fluids.stream();
    }

    @Override
    public SlotDisplay display() {
        return new SlotDisplay.Composite(Arrays.stream(stacks).<SlotDisplay>map(FluidStackSlotDisplay::new).toList());
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public FluidIngredientType<?> getType() {
        return TYPE;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * fluids.hashCode() + components.hashCode()) + Boolean.hashCode(strict);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof DataComponentFluidIngredient other && other.fluids.equals(fluids) && other.components.equals(components)
                              && other.strict == strict;
    }

    public HolderSet<Fluid> fluidSet() {
        return fluids;
    }

    public DataComponentExactPredicate components() {
        return components;
    }

    public boolean isStrict() {
        return strict;
    }

    public static FluidIngredient of(boolean strict, FluidStack stack) {
        return of(strict, stack.getComponents(), stack.getFluidHolder());
    }

    @SafeVarargs
    public static FluidIngredient of(boolean strict, DataComponentMap map, net.minecraft.core.Holder<Fluid>... fluids) {
        return of(strict, DataComponentExactPredicate.allOf(map), fluids);
    }

    @SafeVarargs
    public static FluidIngredient of(boolean strict, DataComponentExactPredicate predicate, Holder<Fluid>... fluids) {
        return of(strict, predicate, HolderSet.direct(fluids));
    }

    @SuppressWarnings("deprecation")
    public static FluidIngredient of(boolean strict, DataComponentExactPredicate predicate, Fluid... fluids) {
        return of(strict, predicate, HolderSet.direct(Arrays.stream(fluids).map(Fluid::builtInRegistryHolder).toList()));
    }

    public static FluidIngredient of(boolean strict, DataComponentExactPredicate predicate, HolderSet<Fluid> fluids) {
        return new DataComponentFluidIngredient(fluids, predicate, strict);
    }
}
