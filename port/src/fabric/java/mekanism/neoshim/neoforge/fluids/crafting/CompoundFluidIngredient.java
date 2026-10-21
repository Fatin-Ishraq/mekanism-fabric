package mekanism.neoshim.neoforge.fluids.crafting;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.stream.Stream;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;

public final class CompoundFluidIngredient extends FluidIngredient {

    public static final MapCodec<CompoundFluidIngredient> CODEC = FluidIngredient.CODEC.listOf()
          .validate(list -> list.isEmpty() ? com.mojang.serialization.DataResult.error(() -> "Compound fluid ingredient must have at least one child")
                                           : com.mojang.serialization.DataResult.success(list))
          .fieldOf("children").xmap(CompoundFluidIngredient::new, CompoundFluidIngredient::children);
    public static final StreamCodec<RegistryFriendlyByteBuf, CompoundFluidIngredient> STREAM_CODEC = FluidIngredient.STREAM_CODEC.apply(ByteBufCodecs.list())
          .map(CompoundFluidIngredient::new, CompoundFluidIngredient::children);
    public static final FluidIngredientType<CompoundFluidIngredient> TYPE = FluidIngredientTypes.register(
          Identifier.fromNamespaceAndPath("neoforge", "compound"), CODEC, STREAM_CODEC);

    private final List<FluidIngredient> children;

    public CompoundFluidIngredient(List<? extends FluidIngredient> children) {
        if (children.isEmpty()) {
            throw new IllegalArgumentException("Compound fluid ingredient must have at least one child");
        }
        this.children = List.copyOf(children);
    }

    public static FluidIngredient of(FluidIngredient... children) {
        if (children.length == 1) {
            return children[0];
        }
        return new CompoundFluidIngredient(List.of(children));
    }

    public static FluidIngredient of(List<FluidIngredient> children) {
        if (children.size() == 1) {
            return children.getFirst();
        }
        return new CompoundFluidIngredient(children);
    }

    @Override
    protected Stream<Holder<Fluid>> generateFluids() {
        return children.stream().flatMap(child -> child.fluids().stream()).distinct();
    }

    @Override
    public boolean test(FluidStack stack) {
        for (FluidIngredient ingredient : children) {
            if (ingredient.test(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSimple() {
        for (FluidIngredient ingredient : children) {
            if (!ingredient.isSimple()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public FluidIngredientType<?> getType() {
        return TYPE;
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
        return this == obj || obj instanceof CompoundFluidIngredient other && other.children.equals(this.children);
    }
}
