package mekanism.neoshim.neoforge.fluids;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

public class FluidStackLinkedSet {

    public static final Hash.Strategy<? super FluidStack> TYPE_AND_COMPONENTS = new Hash.Strategy<>() {
        @Override
        public int hashCode(@Nullable FluidStack stack) {
            return FluidStack.hashFluidAndComponents(stack);
        }

        @Override
        public boolean equals(@Nullable FluidStack first, @Nullable FluidStack second) {
            return first == second || first != null && second != null && first.isEmpty() == second.isEmpty() && FluidStack.isSameFluidSameComponents(first, second);
        }
    };

    public static Set<FluidStack> createTypeAndComponentsSet() {
        return new ObjectLinkedOpenCustomHashSet<>(TYPE_AND_COMPONENTS);
    }
}
