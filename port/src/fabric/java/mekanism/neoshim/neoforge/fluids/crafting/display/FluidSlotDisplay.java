package mekanism.neoshim.neoforge.fluids.crafting.display;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.material.Fluid;

/** Displays a single fluid. Item display factories get the fluid's bucket. */
public record FluidSlotDisplay(Holder<Fluid> fluid) implements SlotDisplay {

    public static final MapCodec<FluidSlotDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
          BuiltInRegistries.FLUID.holderByNameCodec().fieldOf("fluid").forGetter(FluidSlotDisplay::fluid)
    ).apply(instance, FluidSlotDisplay::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidSlotDisplay> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.holderRegistry(Registries.FLUID), FluidSlotDisplay::fluid,
          FluidSlotDisplay::new);
    public static final SlotDisplay.Type<FluidSlotDisplay> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public SlotDisplay.Type<FluidSlotDisplay> type() {
        return TYPE;
    }

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        return resolveFluid(new FluidStack(fluid, mekanism.neoshim.neoforge.fluids.FluidType.BUCKET_VOLUME), factory);
    }

    static <T> Stream<T> resolveFluid(FluidStack stack, DisplayContentsFactory<T> factory) {
        if (factory instanceof ForFluidStacks<T> fluids) {
            return Stream.of(fluids.forStack(stack));
        } else if (factory instanceof DisplayContentsFactory.ForStacks<T> items && !stack.isEmpty()) {
            net.minecraft.world.item.Item bucket = stack.getFluid().getBucket();
            if (bucket != net.minecraft.world.item.Items.AIR) {
                return Stream.of(items.forStack(bucket));
            }
        }
        return Stream.empty();
    }
}
