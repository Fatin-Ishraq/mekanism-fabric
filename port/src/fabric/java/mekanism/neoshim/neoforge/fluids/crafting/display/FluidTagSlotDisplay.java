package mekanism.neoshim.neoforge.fluids.crafting.display;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import mekanism.neoshim.neoforge.fluids.FluidType;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.material.Fluid;

/** Displays every fluid of a tag. */
public record FluidTagSlotDisplay(TagKey<Fluid> tag) implements SlotDisplay {

    public static final MapCodec<FluidTagSlotDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
          TagKey.codec(Registries.FLUID).fieldOf("tag").forGetter(FluidTagSlotDisplay::tag)
    ).apply(instance, FluidTagSlotDisplay::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidTagSlotDisplay> STREAM_CODEC = StreamCodec.composite(
          TagKey.streamCodec(Registries.FLUID), FluidTagSlotDisplay::tag,
          FluidTagSlotDisplay::new);
    public static final SlotDisplay.Type<FluidTagSlotDisplay> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public SlotDisplay.Type<FluidTagSlotDisplay> type() {
        return TYPE;
    }

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        HolderLookup.Provider registries = context.getOptional(SlotDisplayContext.REGISTRIES);
        java.util.Optional<HolderSet.Named<Fluid>> set = registries == null ? BuiltInRegistries.FLUID.get(tag) : registries.lookupOrThrow(Registries.FLUID).get(tag);
        return set.stream().flatMap(HolderSet::stream).flatMap(fluid -> FluidSlotDisplay.resolveFluid(new FluidStack(fluid, FluidType.BUCKET_VOLUME), factory));
    }
}
