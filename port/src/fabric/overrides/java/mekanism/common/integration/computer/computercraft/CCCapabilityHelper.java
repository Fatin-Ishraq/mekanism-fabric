package mekanism.common.integration.computer.computercraft;

import java.util.function.BooleanSupplier;
import mekanism.common.integration.computer.IComputerTile;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister.BlockEntityTypeBuilder;
import mekanism.common.tile.base.CapabilityTileEntity;
import mekanism.neoshim.neoforge.capabilities.RegisterCapabilitiesEvent;

/// FABRIC stub: CC:Tweaked is not available for Fabric 26.1 yet; the upstream integration is compiled out (NOTES.md phase 9). Computer methods are still
/// registered by the common computer API, only the CC peripheral capability is absent.
public class CCCapabilityHelper {

    public static <TILE extends CapabilityTileEntity & IComputerTile> void addCapability(BlockEntityTypeBuilder<TILE> builder, BooleanSupplier supportsComputer) {
    }

    public static void addBoundingComputerCapabilities(RegisterCapabilitiesEvent event) {
    }

    public static void registerApis() {
    }
}
