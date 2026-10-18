package mekanism.neoshim.neoforge.fluids.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.stream.Stream;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import mekanism.neoshim.neoforge.fluids.FluidType;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

public final class DataComponentFluidIngredient extends FluidIngredient {

    public static final MapCodec<DataComponentFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          HolderSetCodec.create(Registries.FLUID, BuiltInRegistries.FLUID.holderByNameCodec(), false).fieldOf("fluids").forGetter(DataComponentFluidIngredient::fluids),
          DataComponentPredicate.CODEC.fieldOf("components").forGetter(DataComponentFluidIngredient::components),
          Codec.BOOL.optionalFieldOf("strict", false).forGetter(DataComponentFluidIngredient::isStrict)
    ).apply(builder, DataComponentFluidIngredient::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, DataComponentFluidIngredient> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.holderSet(Registries.FLUID), DataComponentFluidIngredient::fluids,
          DataComponentPredicate.STREAM_CODEC, DataComponentFluidIngredient::components,
          ByteBufCodecs.BOOL, DataComponentFluidIngredient::isStrict,
          DataComponentFluidIngredient::new);
    public static final FluidIngredientTypes.Type<DataComponentFluidIngredient> TYPE = FluidIngredientTypes.register(
          ResourceLocation.fromNamespaceAndPath("neoforge", "components"), CODEC, STREAM_CODEC);

    private final HolderSet<Fluid> fluids;
    private final DataComponentPredicate components;
    private final boolean strict;
    private final FluidStack[] stacks;

    public DataComponentFluidIngredient(HolderSet<Fluid> fluids, DataComponentPredicate components, boolean strict) {
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
    protected Stream<FluidStack> generateStacks() {
        return Arrays.stream(stacks);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public FluidIngredientTypes.Type<?> getType() {
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

    public HolderSet<Fluid> fluids() {
        return fluids;
    }

    public DataComponentPredicate components() {
        return components;
    }

    public boolean isStrict() {
        return strict;
    }

    public static FluidIngredient of(boolean strict, FluidStack stack) {
        return of(strict, stack.getComponents(), stack.getFluidHolder());
    }

    @SafeVarargs
    public static FluidIngredient of(boolean strict, DataComponentMap map, Holder<Fluid>... fluids) {
        return of(strict, DataComponentPredicate.allOf(map), fluids);
    }

    @SafeVarargs
    public static FluidIngredient of(boolean strict, DataComponentPredicate predicate, Holder<Fluid>... fluids) {
        return of(strict, predicate, HolderSet.direct(fluids));
    }

    @SuppressWarnings("deprecation")
    public static FluidIngredient of(boolean strict, DataComponentPredicate predicate, Fluid... fluids) {
        return of(strict, predicate, HolderSet.direct(Arrays.stream(fluids).map(Fluid::builtInRegistryHolder).toList()));
    }

    public static FluidIngredient of(boolean strict, DataComponentPredicate predicate, HolderSet<Fluid> fluids) {
        return new DataComponentFluidIngredient(fluids, predicate, strict);
    }
}
