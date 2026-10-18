package mekanism.fabric.compat.computer;

import dan200.computercraft.api.peripheral.IPeripheral;
import mekanism.neoshim.neoforge.capabilities.BlockCapability;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * CC: Tweaked's peripheral "capability" on Fabric: CC:T looks peripherals up through a Fabric {@code BlockApiLookup<IPeripheral, Direction>} with the id
 * {@code computercraft:peripheral} ({@code dan200.computercraft.api.peripheral.PeripheralLookup}). Shim block capabilities are backed by the lookup with the same
 * id, so registering against this capability registers with CC:T. Only touched when ComputerCraft is loaded.
 */
public final class PeripheralLookupCapability {

    public static final BlockCapability<IPeripheral, @Nullable Direction> PERIPHERAL = BlockCapability.createSided(
          ResourceLocation.fromNamespaceAndPath("computercraft", "peripheral"), IPeripheral.class);

    private PeripheralLookupCapability() {
    }
}
