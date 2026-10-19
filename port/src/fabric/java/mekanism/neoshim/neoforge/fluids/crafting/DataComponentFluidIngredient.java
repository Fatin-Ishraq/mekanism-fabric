package mekanism.neoshim.neoforge.fluids.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import mekanism.neoshim.neoforge.fluids.FluidType;
import mekanism.neoshim.neoforge.fluids.crafting.display.FluidStackSlotDisplay;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.material.Fluid;

/**
 * Matches fluids whose components match: {@code {"neoforge:ingredient_type": "neoforge:components", "fluids": ..., "components": {...}, "strict": bool}}.
 * Strict ingredients require the stack's component patch to equal the expected components exactly.
 */
public class DataComponentFluidIngredient extends FluidIngredient {

    public static final MapCodec<DataComponentFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          FluidIngredientCodecs.HOLDER_SET_CODEC.fieldOf("fluids").forGetter(DataComponentFluidIngredient::fluidSet),
          DataComponentExactPredicate.CODEC.fieldOf("components").forGetter(DataComponentFluidIngredient::components),
          Codec.BOOL.optionalFieldOf("strict", false).forGetter(DataComponentFluidIngredient::isStrict)
    ).apply(builder, DataComponentFluidIngredient::new));
    static final StreamCodec<RegistryFriendlyByteBuf, DataComponentFluidIngredient> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.holderSet(Registries.FLUID), DataComponentFluidIngredient::fluidSet,
          DataComponentExactPredicate.STREAM_CODEC, DataComponentFluidIngredient::components,
          ByteBufCodecs.BOOL, DataComponentFluidIngredient::isStrict,
          DataComponentFluidIngredient::new);

    private final HolderSet<Fluid> fluids;
    private final DataComponentExactPredicate components;
    private final boolean strict;

    public DataComponentFluidIngredient(HolderSet<Fluid> fluids, DataComponentExactPredicate components, boolean strict) {
        this.fluids = fluids;
        this.components = components;
        this.strict = strict;
    }

    @Override
    public boolean test(FluidStack stack) {
        if (!fluids.contains(stack.getFluidHolder())) {
            return false;
        }
        if (strict) {
            return stack.getComponentsPatch().equals(components.asPatch());
        }
        return components.test(stack);
    }

    @Override
    public Stream<Holder<Fluid>> generateFluids() {
        return fluids.stream();
    }

    @Override
    public SlotDisplay display() {
        return new SlotDisplay.Composite(fluids.stream()
              .map(fluid -> (SlotDisplay) new FluidStackSlotDisplay(new FluidStack(fluid, FluidType.BUCKET_VOLUME, components.asPatch())))
              .toList());
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public FluidIngredientType<?> getType() {
        return FluidIngredientCodecs.Types.DATA_COMPONENT;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fluids, components, strict);
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

    public static <T> FluidIngredient of(boolean strict, DataComponentType<? super T> type, T value, Fluid... fluids) {
        return of(strict, DataComponentExactPredicate.builder().expect(type, value).build(), fluids);
    }

    public static <T> FluidIngredient of(boolean strict, Supplier<? extends DataComponentType<? super T>> type, T value, Fluid... fluids) {
        return of(strict, type.get(), value, fluids);
    }

    public static FluidIngredient of(boolean strict, DataComponentMap map, Fluid... fluids) {
        return of(strict, DataComponentExactPredicate.allOf(map), fluids);
    }

    @SafeVarargs
    public static FluidIngredient of(boolean strict, DataComponentMap map, Holder<Fluid>... fluids) {
        return of(strict, DataComponentExactPredicate.allOf(map), fluids);
    }

    public static FluidIngredient of(boolean strict, DataComponentMap map, HolderSet<Fluid> fluids) {
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
