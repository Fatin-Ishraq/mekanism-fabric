package mekanism.neoshim.neoforge.fluids.crafting.display;

import java.util.stream.Stream;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;

/** Displays a full fluid stack (fluid, amount and components). */
public record FluidStackSlotDisplay(FluidStack stack) implements SlotDisplay {

    public static final com.mojang.serialization.MapCodec<FluidStackSlotDisplay> MAP_CODEC =
          FluidStack.CODEC.fieldOf("stack").xmap(FluidStackSlotDisplay::new, FluidStackSlotDisplay::stack);
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStackSlotDisplay> STREAM_CODEC =
          FluidStack.STREAM_CODEC.map(FluidStackSlotDisplay::new, FluidStackSlotDisplay::stack);

    @Override
    public SlotDisplay.Type<? extends SlotDisplay> type() {
        return FluidDisplays.FLUID_STACK;
    }

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        return factory instanceof ForFluidStacks<T> fluidStacks ? Stream.of(fluidStacks.forStack(stack)) : Stream.empty();
    }
}
