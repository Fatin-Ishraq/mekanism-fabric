package mekanism.neoshim.neoforge.fluids.crafting;

import java.util.stream.Stream;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import mekanism.neoshim.neoforge.fluids.crafting.display.FluidTagSlotDisplay;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.material.Fluid;

/** Matches any fluid of a holder set, regardless of components. */
public class SimpleFluidIngredient extends FluidIngredient {

    private final HolderSet<Fluid> values;

    public SimpleFluidIngredient(HolderSet<Fluid> values) {
        this.values = values;
    }

    @Override
    public boolean test(FluidStack stack) {
        return values.contains(stack.getFluidHolder());
    }

    @Override
    protected Stream<Holder<Fluid>> generateFluids() {
        return values.stream();
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public FluidIngredientType<?> getType() {
        return FluidIngredientCodecs.simpleType();
    }

    @Override
    public SlotDisplay display() {
        return values.unwrapKey().<SlotDisplay>map(FluidTagSlotDisplay::new).orElseGet(super::display);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof SimpleFluidIngredient other && other.values.equals(values);
    }

    public HolderSet<Fluid> fluidSet() {
        return values;
    }

    @Override
    public String toString() {
        return "SimpleFluidIngredient[" + values + "]";
    }
}
