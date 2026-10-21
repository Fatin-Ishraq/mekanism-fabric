package mekanism.neoshim.neoforge.fluids.crafting.display;

import java.util.stream.Stream;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.material.Fluid;

/** Displays a single fluid. */
public record FluidSlotDisplay(Holder<Fluid> fluid) implements SlotDisplay {

    public static final com.mojang.serialization.MapCodec<FluidSlotDisplay> MAP_CODEC =
          FluidStack.FLUID_NON_EMPTY_CODEC.fieldOf("fluid").xmap(FluidSlotDisplay::new, FluidSlotDisplay::fluid);
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidSlotDisplay> STREAM_CODEC =
          ByteBufCodecs.holderRegistry(Registries.FLUID).map(FluidSlotDisplay::new, FluidSlotDisplay::fluid);

    @Override
    public SlotDisplay.Type<? extends SlotDisplay> type() {
        return FluidDisplays.FLUID;
    }

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        return factory instanceof ForFluidStacks<T> fluidStacks ? Stream.of(fluidStacks.forStack(fluid)) : Stream.empty();
    }
}
