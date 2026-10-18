package mekanism.neoshim.neoforge.fluids.crafting;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

/**
 * Fluid equivalent of an ingredient. Fabric has no fluid ingredients, so this is a self-contained implementation using NeoForge's JSON format:
 * {@code {"fluid": id}}, {@code {"tag": id}}, or {@code {"type": "neoforge:<type>", ...}}, or a list (compound).
 */
public abstract class FluidIngredient implements Predicate<FluidStack> {

    public static final MapCodec<FluidIngredient> MAP_CODEC_NONEMPTY = FluidIngredientTypes.MAP_CODEC_NONEMPTY;
    public static final Codec<List<FluidIngredient>> LIST_CODEC_NON_EMPTY = Codec.lazyInitialized(() -> MAP_CODEC_NONEMPTY.codec().listOf()
          .validate(list -> list.isEmpty() ? DataResult.error(() -> "Fluid ingredient cannot be empty") : DataResult.success(list)));
    public static final Codec<FluidIngredient> CODEC_NON_EMPTY = Codec.lazyInitialized(() -> Codec.either(LIST_CODEC_NON_EMPTY, MAP_CODEC_NONEMPTY.codec())
          .xmap(either -> either.map(CompoundFluidIngredient::of, i -> i),
                ingredient -> ingredient instanceof CompoundFluidIngredient compound ? Either.left(compound.children()) : Either.right(ingredient)));
    public static final Codec<FluidIngredient> CODEC = Codec.lazyInitialized(() -> Codec.either(Codec.list(MAP_CODEC_NONEMPTY.codec()), MAP_CODEC_NONEMPTY.codec())
          .xmap(either -> either.map(list -> list.isEmpty() ? empty() : CompoundFluidIngredient.of(list), i -> i),
                ingredient -> ingredient instanceof EmptyFluidIngredient ? Either.left(List.of())
                                                                          : ingredient instanceof CompoundFluidIngredient compound ? Either.left(compound.children())
                                                                                                                                   : Either.right(ingredient)));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidIngredient> STREAM_CODEC = FluidIngredientTypes.STREAM_CODEC;

    @Nullable
    private FluidStack[] stacks;

    public final FluidStack[] getStacks() {
        if (stacks == null) {
            stacks = generateStacks().toArray(FluidStack[]::new);
        }
        return stacks;
    }

    @Override
    public abstract boolean test(FluidStack stack);

    protected abstract Stream<FluidStack> generateStacks();

    public abstract boolean isSimple();

    public abstract FluidIngredientTypes.Type<?> getType();

    public boolean isEmpty() {
        return this == empty();
    }

    public boolean hasNoFluids() {
        return getStacks().length == 0;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    public static FluidIngredient empty() {
        return EmptyFluidIngredient.INSTANCE;
    }

    public static FluidIngredient of() {
        return empty();
    }

    public static FluidIngredient of(FluidStack... fluids) {
        return of(Arrays.stream(fluids).map(FluidStack::getFluid));
    }

    public static FluidIngredient of(Fluid... fluids) {
        return of(Arrays.stream(fluids));
    }

    private static FluidIngredient of(Stream<Fluid> fluids) {
        return CompoundFluidIngredient.of(fluids.map(FluidIngredient::single));
    }

    public static FluidIngredient single(FluidStack stack) {
        return single(stack.getFluid());
    }

    @SuppressWarnings("deprecation")
    public static FluidIngredient single(Fluid fluid) {
        return single(fluid.builtInRegistryHolder());
    }

    public static FluidIngredient single(Holder<Fluid> holder) {
        return new SingleFluidIngredient(holder);
    }

    public static FluidIngredient tag(TagKey<Fluid> tag) {
        return new TagFluidIngredient(tag);
    }

    public static FluidIngredient of(HolderSet<Fluid> set) {
        return set.unwrapKey().<FluidIngredient>map(TagFluidIngredient::new)
              .orElseGet(() -> CompoundFluidIngredient.of(set.stream().map(FluidIngredient::single)));
    }

    static boolean isNotEmptyFluid(Holder<Fluid> holder) {
        return !holder.is(Fluids.EMPTY.builtInRegistryHolder());
    }
}
