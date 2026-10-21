package mekanism.neoshim.neoforge.capabilities;

import mekanism.neoshim.neoforge.transfer.ResourceHandler;
import mekanism.neoshim.neoforge.transfer.access.ItemAccess;
import mekanism.neoshim.neoforge.transfer.energy.EnergyHandler;
import mekanism.neoshim.neoforge.transfer.fluid.FluidResource;
import mekanism.neoshim.neoforge.transfer.item.ItemResource;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * The standard NeoForge capabilities (26.1 layout: resource handlers). Mekanism's handlers are exposed to Fabric's Transfer API / Team Reborn Energy, and foreign
 * Fabric storages are bridged in, by {@link mekanism.fabric.compat.CapabilityBridge}.
 */
public final class Capabilities {

    public static final class Item {

        public static final BlockCapability<ResourceHandler<ItemResource>, @Nullable Direction> BLOCK = BlockCapability.createSided(create("item_handler"),
              ResourceHandler.asClass());
        public static final EntityCapability<ResourceHandler<ItemResource>, Void> ENTITY = EntityCapability.createVoid(create("item_handler"),
              ResourceHandler.asClass());
        public static final EntityCapability<ResourceHandler<ItemResource>, @Nullable Direction> ENTITY_AUTOMATION = EntityCapability.createSided(
              create("item_handler_automation"), ResourceHandler.asClass());
        public static final ItemCapability<ResourceHandler<ItemResource>, ItemAccess> ITEM = ItemCapability.create(create("item_handler"),
              ResourceHandler.asClass(), ItemAccess.class);

        private Item() {
        }
    }

    public static final class Fluid {

        public static final BlockCapability<ResourceHandler<FluidResource>, @Nullable Direction> BLOCK = BlockCapability.createSided(create("fluid_handler"),
              ResourceHandler.asClass());
        public static final EntityCapability<ResourceHandler<FluidResource>, @Nullable Direction> ENTITY = EntityCapability.createSided(create("fluid_handler"),
              ResourceHandler.asClass());
        public static final ItemCapability<ResourceHandler<FluidResource>, ItemAccess> ITEM = ItemCapability.create(create("fluid_handler"),
              ResourceHandler.asClass(), ItemAccess.class);

        private Fluid() {
        }
    }

    public static final class Energy {

        public static final BlockCapability<EnergyHandler, @Nullable Direction> BLOCK = BlockCapability.createSided(create("energy"), EnergyHandler.class);
        public static final EntityCapability<EnergyHandler, @Nullable Direction> ENTITY = EntityCapability.createSided(create("energy"), EnergyHandler.class);
        public static final ItemCapability<EnergyHandler, ItemAccess> ITEM = ItemCapability.create(create("energy"), EnergyHandler.class, ItemAccess.class);

        private Energy() {
        }
    }

    private static Identifier create(String path) {
        return Identifier.fromNamespaceAndPath("neoforge", path);
    }

    private Capabilities() {
    }
}
