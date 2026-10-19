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
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.material.Fluid;

/** Union of fluid ingredients: {@code {"neoforge:ingredient_type": "neoforge:compound", "children": [...]}}. */
public final class CompoundFluidIngredient extends FluidIngredient {

    public static final MapCodec<CompoundFluidIngredient> CODEC = ExtraCodecs.nonEmptyList(Codec.lazyInitialized(() -> FluidIngredient.CODEC).listOf())
          .xmap(CompoundFluidIngredient::new, CompoundFluidIngredient::children).fieldOf("children");
    static final StreamCodec<RegistryFriendlyByteBuf, CompoundFluidIngredient> STREAM_CODEC = FluidIngredientCodecs.lazyStreamCodec()
          .apply(ByteBufCodecs.list()).map(CompoundFluidIngredient::new, CompoundFluidIngredient::children);

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
        if (children.size() == 1) {
            return children.getFirst();
        }
        return new CompoundFluidIngredient(children);
    }

    @Override
    public Stream<Holder<Fluid>> generateFluids() {
        return children.stream().flatMap(child -> child.fluids().stream());
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
        return FluidIngredientCodecs.Types.COMPOUND;
    }

    @Override
    public SlotDisplay display() {
        return new SlotDisplay.Composite(children.stream().map(FluidIngredient::display).toList());
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
