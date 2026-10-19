package mekanism.neoshim.neoforge.fluids.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.stream.Stream;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;

/** Fluids matched by every child: {@code {"neoforge:ingredient_type": "neoforge:intersection", "children": [...]}}. */
public final class IntersectionFluidIngredient extends FluidIngredient {

    public static final MapCodec<IntersectionFluidIngredient> CODEC = ExtraCodecs.nonEmptyList(Codec.lazyInitialized(() -> FluidIngredient.CODEC).listOf())
          .xmap(IntersectionFluidIngredient::new, IntersectionFluidIngredient::children).fieldOf("children");
    static final StreamCodec<RegistryFriendlyByteBuf, IntersectionFluidIngredient> STREAM_CODEC = FluidIngredientCodecs.lazyStreamCodec()
          .apply(ByteBufCodecs.list()).map(IntersectionFluidIngredient::new, IntersectionFluidIngredient::children);

    private final List<FluidIngredient> children;

    public IntersectionFluidIngredient(List<FluidIngredient> children) {
        if (children.isEmpty()) {
            throw new IllegalArgumentException("Intersection fluid ingredient must have at least one child");
        }
        this.children = List.copyOf(children);
    }

    public static FluidIngredient of(FluidIngredient... children) {
        if (children.length == 1) {
            return children[0];
        }
        return new IntersectionFluidIngredient(List.of(children));
    }

    @Override
    public boolean test(FluidStack stack) {
        for (FluidIngredient child : children) {
            if (!child.test(stack)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Stream<Holder<Fluid>> generateFluids() {
        List<FluidIngredient> rest = children.subList(1, children.size());
        return children.getFirst().fluids().stream().filter(fluid -> rest.stream().allMatch(child -> child.fluids().contains(fluid)));
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public FluidIngredientType<?> getType() {
        return FluidIngredientCodecs.Types.INTERSECTION;
    }

    public List<FluidIngredient> children() {
        return children;
    }

    @Override
    public int hashCode() {
        return children.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof IntersectionFluidIngredient other && other.children.equals(this.children);
    }
}
