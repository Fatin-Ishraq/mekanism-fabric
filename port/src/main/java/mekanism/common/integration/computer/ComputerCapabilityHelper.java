package mekanism.common.integration.computer;

import java.util.function.BooleanSupplier;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister.BlockEntityTypeBuilder;
import mekanism.common.tile.base.CapabilityTileEntity;
import mekanism.neoshim.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ComputerCapabilityHelper {

    public static <TILE extends CapabilityTileEntity & IComputerTile> void addComputerCapabilities(BlockEntityTypeBuilder<TILE> builder, BooleanSupplier supportsComputer) {
        //FABRIC: ComputerCraft peripheral registration (CCCapabilityHelper) is TODO; OpenComputers2 does not exist on Fabric
    }

    public static void addBoundingComputerCapabilities(RegisterCapabilitiesEvent event) {
        //FABRIC: see addComputerCapabilities
    }
}