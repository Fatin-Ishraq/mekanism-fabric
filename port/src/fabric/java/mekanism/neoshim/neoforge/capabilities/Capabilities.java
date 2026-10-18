package mekanism.neoshim.neoforge.capabilities;

import mekanism.neoshim.neoforge.energy.IEnergyStorage;
import mekanism.neoshim.neoforge.fluids.capability.IFluidHandler;
import mekanism.neoshim.neoforge.fluids.capability.IFluidHandlerItem;
import mekanism.neoshim.neoforge.items.IItemHandler;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * The standard NeoForge capabilities. Their lookups use NeoForge's ids; foreign Fabric storages are bridged in via fallback providers and
 * Mekanism's handlers are exposed to Fabric's Transfer API / TR Energy by {@link mekanism.fabric.compat.CapabilityBridge}.
 */
public final class Capabilities {

    public static final class EnergyStorage {

        public static final BlockCapability<IEnergyStorage, @Nullable Direction> BLOCK = BlockCapability.createSided(create("energy"), IEnergyStorage.class);
        public static final EntityCapability<IEnergyStorage, @Nullable Direction> ENTITY = EntityCapability.createSided(create("energy"), IEnergyStorage.class);
        public static final ItemCapability<IEnergyStorage, Void> ITEM = ItemCapability.createVoid(create("energy"), IEnergyStorage.class);

        private EnergyStorage() {
        }
    }

    public static final class FluidHandler {

        public static final BlockCapability<IFluidHandler, @Nullable Direction> BLOCK = BlockCapability.createSided(create("fluid_handler"), IFluidHandler.class);
        public static final EntityCapability<IFluidHandler, @Nullable Direction> ENTITY = EntityCapability.createSided(create("fluid_handler"), IFluidHandler.class);
        public static final ItemCapability<IFluidHandlerItem, Void> ITEM = ItemCapability.createVoid(create("fluid_handler"), IFluidHandlerItem.class);

        private FluidHandler() {
        }
    }

    public static final class ItemHandler {

        public static final BlockCapability<IItemHandler, @Nullable Direction> BLOCK = BlockCapability.createSided(create("item_handler"), IItemHandler.class);
        public static final EntityCapability<IItemHandler, Void> ENTITY = EntityCapability.createVoid(create("item_handler"), IItemHandler.class);
        public static final EntityCapability<IItemHandler, @Nullable Direction> ENTITY_AUTOMATION = EntityCapability.createSided(create("item_handler_automation"),
              IItemHandler.class);
        public static final ItemCapability<IItemHandler, Void> ITEM = ItemCapability.createVoid(create("item_handler"), IItemHandler.class);

        private ItemHandler() {
        }
    }

    private static ResourceLocation create(String path) {
        return ResourceLocation.fromNamespaceAndPath("neoforge", path);
    }

    private Capabilities() {
    }
}
