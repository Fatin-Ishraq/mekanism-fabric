package mekanism.neoshim.neoforge.fluids.crafting.display;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;

/** Displays a fluid stack (with amount and components). */
public record FluidStackSlotDisplay(FluidStack stack) implements SlotDisplay {

    public static final MapCodec<FluidStackSlotDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
          FluidStack.CODEC.fieldOf("fluid").forGetter(FluidStackSlotDisplay::stack)
    ).apply(instance, FluidStackSlotDisplay::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStackSlotDisplay> STREAM_CODEC = StreamCodec.composite(
          FluidStack.STREAM_CODEC, FluidStackSlotDisplay::stack,
          FluidStackSlotDisplay::new);
    public static final SlotDisplay.Type<FluidStackSlotDisplay> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public SlotDisplay.Type<FluidStackSlotDisplay> type() {
        return TYPE;
    }

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        return FluidSlotDisplay.resolveFluid(stack.copy(), factory);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof FluidStackSlotDisplay other && FluidStack.matches(stack, other.stack);
    }

    @Override
    public int hashCode() {
        return FluidStack.hashFluidAndComponents(stack) * 31 + stack.getAmount();
    }
}
