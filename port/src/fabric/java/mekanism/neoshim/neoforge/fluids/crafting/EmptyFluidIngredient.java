package mekanism.neoshim.neoforge.fluids.crafting;

import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public final class EmptyFluidIngredient extends FluidIngredient {

    public static final EmptyFluidIngredient INSTANCE = new EmptyFluidIngredient();
    public static final MapCodec<EmptyFluidIngredient> CODEC = MapCodec.unit(INSTANCE);
    public static final FluidIngredientTypes.Type<EmptyFluidIngredient> TYPE = FluidIngredientTypes.register(
          Identifier.fromNamespaceAndPath("neoforge", "empty"), CODEC, StreamCodec.unit(INSTANCE));

    private EmptyFluidIngredient() {
    }

    @Override
    public boolean test(FluidStack stack) {
        return stack.isEmpty();
    }

    @Override
    protected Stream<FluidStack> generateStacks() {
        return Stream.empty();
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public FluidIngredientTypes.Type<?> getType() {
        return TYPE;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
}
