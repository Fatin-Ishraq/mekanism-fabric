package mekanism.neoshim.neoforge.fluids.crafting.display;

import java.util.stream.Stream;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.context.ContextMap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

/** Displays every fluid in a tag. */
public record FluidTagSlotDisplay(TagKey<Fluid> tag) implements SlotDisplay {

    public static final com.mojang.serialization.MapCodec<FluidTagSlotDisplay> MAP_CODEC =
          TagKey.codec(net.minecraft.core.registries.Registries.FLUID).fieldOf("tag").xmap(FluidTagSlotDisplay::new, FluidTagSlotDisplay::tag);
    public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, FluidTagSlotDisplay> STREAM_CODEC =
          net.minecraft.resources.Identifier.STREAM_CODEC.<net.minecraft.network.RegistryFriendlyByteBuf>cast()
                .map(id -> new FluidTagSlotDisplay(TagKey.create(net.minecraft.core.registries.Registries.FLUID, id)),
                      display -> display.tag.location());

    @Override
    public SlotDisplay.Type<? extends SlotDisplay> type() {
        return FluidDisplays.FLUID_TAG;
    }

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        if (factory instanceof ForFluidStacks<T> fluidStacks) {
            HolderLookup.Provider registries = context.getOptional(SlotDisplayContext.REGISTRIES);
            if (registries != null) {
                return registries.lookupOrThrow(net.minecraft.core.registries.Registries.FLUID).get(tag)
                      .map(named -> named.stream().map(fluidStacks::forStack))
                      .orElseGet(Stream::empty);
            }
        }
        return Stream.empty();
    }
}
