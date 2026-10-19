package mekanism.common.integration.computer;

import java.util.function.BooleanSupplier;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister.BlockEntityTypeBuilder;
import mekanism.common.tile.base.CapabilityTileEntity;
import mekanism.neoshim.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ComputerCapabilityHelper {

    public static <TILE extends CapabilityTileEntity & IComputerTile> void addComputerCapabilities(BlockEntityTypeBuilder<TILE> builder, BooleanSupplier supportsComputer) {
        if (Mekanism.hooks.computerCraft.isLoaded()) {
            //If ComputerCraft is loaded add the capability for it
            //FABRIC: 26.1 phase 1 - the CC: Tweaked integration is compiled out
            //CCCapabilityHelper.addCapability(builder, supportsComputer);
        }
    }

    public static void addBoundingComputerCapabilities(RegisterCapabilitiesEvent event) {
        if (Mekanism.hooks.computerCraft.isLoaded()) {
            //If ComputerCraft is loaded add the capability for it
            //FABRIC: 26.1 phase 1 - the CC: Tweaked integration is compiled out
            //CCCapabilityHelper.addBoundingComputerCapabilities(event);
        }
    }
}