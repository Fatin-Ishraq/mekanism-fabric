package mekanism.neoshim.neoforge.fluids.crafting;

import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public final class CompoundFluidIngredient extends FluidIngredient {

    public static final MapCodec<CompoundFluidIngredient> CODEC = FluidIngredient.LIST_CODEC_NON_EMPTY.xmap(CompoundFluidIngredient::new, CompoundFluidIngredient::children)
          .fieldOf("children");
    public static final StreamCodec<RegistryFriendlyByteBuf, CompoundFluidIngredient> STREAM_CODEC = FluidIngredient.STREAM_CODEC.apply(ByteBufCodecs.list())
          .map(CompoundFluidIngredient::new, CompoundFluidIngredient::children);
    public static final FluidIngredientTypes.Type<CompoundFluidIngredient> TYPE = FluidIngredientTypes.register(
          ResourceLocation.fromNamespaceAndPath("neoforge", "compound"), CODEC, STREAM_CODEC);

    private final List<FluidIngredient> children;

    public CompoundFluidIngredient(List<? extends FluidIngredient> children) {
        if (children.isEmpty()) {
            throw new IllegalArgumentException("Compound fluid ingredient must have at least one child");
        }
        this.children = List.copyOf(children);
    }

    public static FluidIngredient of(FluidIngredient... children) {
        return of(List.of(children));
    }

    public static FluidIngredient of(List<FluidIngredient> children) {
        if (children.isEmpty()) {
            return FluidIngredient.empty();
        } else if (children.size() == 1) {
            return children.getFirst();
        }
        return new CompoundFluidIngredient(children);
    }

    public static FluidIngredient of(Stream<FluidIngredient> stream) {
        List<FluidIngredient> list = new ArrayList<>();
        stream.forEach(list::add);
        return of(list);
    }

    @Override
    protected Stream<FluidStack> generateStacks() {
        return children.stream().flatMap(child -> Stream.of(child.getStacks()));
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
    public FluidIngredientTypes.Type<?> getType() {
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
