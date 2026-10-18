package mekanism.neoshim.neoforge.common;

import mekanism.neoshim.neoforge.fluids.FluidType;
import mekanism.neoshim.neoforge.registries.DeferredHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.material.Fluid;

/**
 * NeoForge's extra attributes and the optional milk fluid. The attributes are registered by the port under the {@code neoforge} namespace
 * (see {@code mekanism.fabric.compat.NeoForgeAttributes}); their gameplay effect is wired up by mixins (NOTES.md "Hook status").
 */
public class NeoForgeMod {

    public static final Holder<Attribute> SWIM_SPEED = DeferredHolder.create(Registries.ATTRIBUTE, ResourceLocation.fromNamespaceAndPath("neoforge", "swim_speed"));
    public static final Holder<Attribute> CREATIVE_FLIGHT = DeferredHolder.create(Registries.ATTRIBUTE, ResourceLocation.fromNamespaceAndPath("neoforge", "creative_flight"));
    public static final DeferredHolder<Fluid, Fluid> MILK = DeferredHolder.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("mekanism_fabric", "milk"));
    public static final Holder<FluidType> WATER_TYPE = Holder.direct(FluidType.VanillaTypes.WATER);
    public static final Holder<FluidType> LAVA_TYPE = Holder.direct(FluidType.VanillaTypes.LAVA);
    public static final Holder<FluidType> EMPTY_TYPE = Holder.direct(FluidType.VanillaTypes.EMPTY);

    private static boolean enableMilkFluid = false;

    public static void enableMilkFluid() {
        enableMilkFluid = true;
    }

    public static boolean isMilkFluidEnabled() {
        return enableMilkFluid;
    }
}
