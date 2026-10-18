package mekanism.neoshim.neoforge.fluids.crafting;

import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import mekanism.neoshim.neoforge.fluids.FluidType;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

public final class SingleFluidIngredient extends FluidIngredient {

    public static final MapCodec<SingleFluidIngredient> CODEC = FluidStack.FLUID_NON_EMPTY_CODEC.xmap(SingleFluidIngredient::new, SingleFluidIngredient::fluid)
          .fieldOf("fluid");
    public static final StreamCodec<RegistryFriendlyByteBuf, SingleFluidIngredient> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.FLUID)
          .map(SingleFluidIngredient::new, SingleFluidIngredient::fluid);
    public static final FluidIngredientTypes.Type<SingleFluidIngredient> TYPE = FluidIngredientTypes.register(
          ResourceLocation.fromNamespaceAndPath("neoforge", "single"), CODEC, STREAM_CODEC);

    private final Holder<Fluid> fluid;

    public SingleFluidIngredient(Holder<Fluid> fluid) {
        if (!isNotEmptyFluid(fluid)) {
            throw new IllegalStateException("SingleFluidIngredient must not be constructed with minecraft:empty, use FluidIngredient.empty() instead!");
        }
        this.fluid = fluid;
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return fluidStack.is(fluid);
    }

    @Override
    protected Stream<FluidStack> generateStacks() {
        return Stream.of(new FluidStack(fluid, FluidType.BUCKET_VOLUME));
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
        return this.fluid.value().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof SingleFluidIngredient other && other.fluid.is(this.fluid);
    }

    public Holder<Fluid> fluid() {
        return fluid;
    }
}
