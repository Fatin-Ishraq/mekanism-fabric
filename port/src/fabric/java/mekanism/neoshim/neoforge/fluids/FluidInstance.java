package mekanism.neoshim.neoforge.fluids;

import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.world.level.material.Fluid;

/** Something that is an amount of a fluid with components (a fluid stack or template). */
public interface FluidInstance extends TypedInstance<Fluid>, DataComponentGetter {

    String FIELD_ID = "id";
    String FIELD_AMOUNT = "amount";
    String FIELD_COMPONENTS = "components";

    int amount();

    default FluidType getFluidType() {
        return typeHolder().value().getFluidType();
    }

    default boolean is(FluidType type) {
        return getFluidType() == type;
    }
}
