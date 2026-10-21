package mekanism.neoshim.neoforge.fluids.crafting.display;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.display.SlotDisplay;

/** Registers the custom fluid slot display types. Called during mod initialization, before the registries freeze. */
public final class FluidDisplays {

    public static final SlotDisplay.Type<FluidSlotDisplay> FLUID = Registry.register(BuiltInRegistries.SLOT_DISPLAY,
          Identifier.fromNamespaceAndPath("neoforge", "fluid"), new SlotDisplay.Type<>(FluidSlotDisplay.MAP_CODEC, FluidSlotDisplay.STREAM_CODEC));
    public static final SlotDisplay.Type<FluidStackSlotDisplay> FLUID_STACK = Registry.register(BuiltInRegistries.SLOT_DISPLAY,
          Identifier.fromNamespaceAndPath("neoforge", "fluid_stack"), new SlotDisplay.Type<>(FluidStackSlotDisplay.MAP_CODEC, FluidStackSlotDisplay.STREAM_CODEC));
    public static final SlotDisplay.Type<FluidTagSlotDisplay> FLUID_TAG = Registry.register(BuiltInRegistries.SLOT_DISPLAY,
          Identifier.fromNamespaceAndPath("neoforge", "fluid_tag"), new SlotDisplay.Type<>(FluidTagSlotDisplay.MAP_CODEC, FluidTagSlotDisplay.STREAM_CODEC));

    public static void init() {
    }

    private FluidDisplays() {
    }
}
