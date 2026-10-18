package mekanism.common.integration.computer;

import java.util.function.BooleanSupplier;
import mekanism.common.Mekanism;
import mekanism.common.integration.computer.computercraft.CCCapabilityHelper;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister.BlockEntityTypeBuilder;
import mekanism.common.tile.base.CapabilityTileEntity;
import mekanism.neoshim.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ComputerCapabilityHelper {

    public static <TILE extends CapabilityTileEntity & IComputerTile> void addComputerCapabilities(BlockEntityTypeBuilder<TILE> builder, BooleanSupplier supportsComputer) {
        if (Mekanism.hooks.computerCraft.isLoaded()) {
            //If ComputerCraft is loaded add the capability for it
            CCCapabilityHelper.addCapability(builder, supportsComputer);
        }
        //FABRIC: OpenComputers2 does not exist on Fabric
    }

    public static void addBoundingComputerCapabilities(RegisterCapabilitiesEvent event) {
        if (Mekanism.hooks.computerCraft.isLoaded()) {
            //If ComputerCraft is loaded add the capability for it
            CCCapabilityHelper.addBoundingComputerCapabilities(event);
        }
        //FABRIC: OpenComputers2 does not exist on Fabric
    }
}