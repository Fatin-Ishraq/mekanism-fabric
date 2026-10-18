package mekanism.fabric.compat;

import mekanism.fabric.compat.transfer.EnergyAdapters;
import mekanism.fabric.compat.transfer.FabricItemAccess;
import mekanism.fabric.compat.transfer.ItemAccessContext;
import mekanism.fabric.compat.transfer.ResourceHandlerStorage;
import mekanism.fabric.compat.transfer.StorageResourceHandler;
import mekanism.neoshim.neoforge.capabilities.BlockCapability;
import mekanism.neoshim.neoforge.capabilities.Capabilities;
import mekanism.neoshim.neoforge.capabilities.EntityCapability;
import mekanism.neoshim.neoforge.capabilities.ItemCapability;
import mekanism.neoshim.neoforge.transfer.ResourceHandler;
import mekanism.neoshim.neoforge.transfer.access.ItemAccess;
import mekanism.neoshim.neoforge.transfer.energy.EnergyHandler;
import mekanism.neoshim.neoforge.transfer.fluid.FluidResource;
import mekanism.neoshim.neoforge.transfer.item.ItemResource;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

/**
 * Interop between the NeoForge-style capabilities and Fabric's Transfer API / Team Reborn Energy (NOTES.md D4). On 26.1 both sides are transactional and share
 * Fabric's transaction stack, so wrappers translate directly:
 * <ul>
 *     <li>Export: when a provider for the standard item/fluid/energy block or item capability is registered, the same block (entity) or item also gets a Fabric
 *     {@link ItemStorage}/{@link FluidStorage}/{@link EnergyStorage} provider wrapping it.</li>
 *     <li>Import: the standard capabilities get fallback providers wrapping any foreign Fabric storage (vanilla containers, cauldrons, buckets, other mods),
 *     unwrapping our own adapters to avoid chains.</li>
 * </ul>
 * Fluid amounts: 1 mB = {@value FLUID_SCALE} droplets.
 */
public final class CapabilityBridge {

    public static final long FLUID_SCALE = FluidConstants.BUCKET / 1000;

    private static boolean fallbacksRegistered;
    /** Set while an export provider queries the NeoForge-side capability, so the import fallback cannot bounce back to Fabric (loop). */
    private static final ThreadLocal<Boolean> EXPORTING = ThreadLocal.withInitial(() -> false);

    private CapabilityBridge() {
    }

    // ---- wrappers ----

    public static Storage<ItemVariant> itemStorage(ResourceHandler<ItemResource> handler) {
        return handler instanceof StorageResourceHandler<?, ?> foreign ? castStorage(foreign.storage())
                                                                        : new ResourceHandlerStorage<>(handler, ItemResource::of, ItemResource::toVariant, 1);
    }

    public static Storage<FluidVariant> fluidStorage(ResourceHandler<FluidResource> handler) {
        return handler instanceof StorageResourceHandler<?, ?> foreign ? castStorage(foreign.storage())
                                                                        : new ResourceHandlerStorage<>(handler, FluidResource::of, FluidResource::toVariant, FLUID_SCALE);
    }

    @SuppressWarnings("unchecked")
    public static ResourceHandler<ItemResource> itemHandler(Storage<ItemVariant> storage) {
        if (storage instanceof ResourceHandlerStorage<?, ?> ours) {
            return (ResourceHandler<ItemResource>) ours.handler();
        }
        return new StorageResourceHandler<>(storage, variant -> variant == null ? ItemResource.EMPTY : ItemResource.of(variant), ItemResource::toVariant, 1);
    }

    @SuppressWarnings("unchecked")
    public static ResourceHandler<FluidResource> fluidHandler(Storage<FluidVariant> storage) {
        if (storage instanceof ResourceHandlerStorage<?, ?> ours) {
            return (ResourceHandler<FluidResource>) ours.handler();
        }
        return new StorageResourceHandler<>(storage, variant -> variant == null ? FluidResource.EMPTY : FluidResource.of(variant), FluidResource::toVariant,
              FLUID_SCALE);
    }

    public static EnergyStorage energyStorage(EnergyHandler handler) {
        return handler instanceof EnergyAdapters.FromTeamReborn(EnergyStorage foreign) ? foreign : new EnergyAdapters.ToTeamReborn(handler);
    }

    public static EnergyHandler energyHandler(EnergyStorage storage) {
        return storage instanceof EnergyAdapters.ToTeamReborn(EnergyHandler ours) ? ours : new EnergyAdapters.FromTeamReborn(storage);
    }

    @SuppressWarnings("unchecked")
    private static <V> Storage<V> castStorage(Storage<?> storage) {
        return (Storage<V>) storage;
    }

    // ---- import fallbacks ----

    /** Registers the import fallbacks; called once by the bootstrap before RegisterCapabilitiesEvent. */
    public static void registerFallbacks() {
        if (fallbacksRegistered) {
            return;
        }
        fallbacksRegistered = true;
        Capabilities.Item.BLOCK.lookup().registerFallback((level, pos, state, be, side) -> {
            if (EXPORTING.get()) {
                return null;
            }
            Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, state, be, side);
            return storage == null ? null : itemHandler(storage);
        });
        Capabilities.Fluid.BLOCK.lookup().registerFallback((level, pos, state, be, side) -> {
            if (EXPORTING.get()) {
                return null;
            }
            Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, pos, state, be, side);
            return storage == null ? null : fluidHandler(storage);
        });
        Capabilities.Energy.BLOCK.lookup().registerFallback((level, pos, state, be, side) -> {
            if (EXPORTING.get()) {
                return null;
            }
            EnergyStorage storage = EnergyStorage.SIDED.find(level, pos, state, be, side);
            return storage == null ? null : energyHandler(storage);
        });
        Capabilities.Fluid.ITEM.lookup().registerFallback((stack, access) -> {
            if (EXPORTING.get() || access == null) {
                return null;
            }
            Storage<FluidVariant> storage = FluidStorage.ITEM.find(stack, ItemAccessContext.of(access));
            return storage == null ? null : fluidHandler(storage);
        });
        Capabilities.Energy.ITEM.lookup().registerFallback((stack, access) -> {
            if (EXPORTING.get() || access == null) {
                return null;
            }
            EnergyStorage storage = EnergyStorage.ITEM.find(stack, ItemAccessContext.of(access));
            return storage == null ? null : energyHandler(storage);
        });
    }

    // ---- export ----

    @Nullable
    private static <T, C> T query(BlockCapability<T, C> capability, Level level, BlockPos pos, BlockState state, @Nullable BlockEntity be, C context) {
        boolean previous = EXPORTING.get();
        EXPORTING.set(true);
        try {
            return capability.getCapability(level, pos, state, be, context);
        } finally {
            EXPORTING.set(previous);
        }
    }

    @Nullable
    private static <T> T queryItem(ItemCapability<T, ItemAccess> capability, ItemStack stack, ContainerItemContext context) {
        boolean previous = EXPORTING.get();
        EXPORTING.set(true);
        try {
            return capability.getCapability(stack, new FabricItemAccess(context));
        } finally {
            EXPORTING.set(previous);
        }
    }

    public static void onBlockRegistered(BlockCapability<?, ?> capability, Block... blocks) {
        if (capability == Capabilities.Item.BLOCK) {
            ItemStorage.SIDED.registerForBlocks((level, pos, state, be, side) -> {
                ResourceHandler<ItemResource> handler = query(Capabilities.Item.BLOCK, level, pos, state, be, side);
                return handler == null ? null : itemStorage(handler);
            }, blocks);
        } else if (capability == Capabilities.Fluid.BLOCK) {
            FluidStorage.SIDED.registerForBlocks((level, pos, state, be, side) -> {
                ResourceHandler<FluidResource> handler = query(Capabilities.Fluid.BLOCK, level, pos, state, be, side);
                return handler == null ? null : fluidStorage(handler);
            }, blocks);
        } else if (capability == Capabilities.Energy.BLOCK) {
            EnergyStorage.SIDED.registerForBlocks((level, pos, state, be, side) -> {
                EnergyHandler handler = query(Capabilities.Energy.BLOCK, level, pos, state, be, side);
                return handler == null ? null : energyStorage(handler);
            }, blocks);
        }
    }

    public static void onBlockEntityRegistered(BlockCapability<?, ?> capability, BlockEntityType<?> type) {
        if (capability == Capabilities.Item.BLOCK) {
            ItemStorage.SIDED.registerForBlockEntity((be, side) -> {
                ResourceHandler<ItemResource> handler = query(Capabilities.Item.BLOCK, be.getLevel(), be.getBlockPos(), be.getBlockState(), be, side);
                return handler == null ? null : itemStorage(handler);
            }, type);
        } else if (capability == Capabilities.Fluid.BLOCK) {
            FluidStorage.SIDED.registerForBlockEntity((be, side) -> {
                ResourceHandler<FluidResource> handler = query(Capabilities.Fluid.BLOCK, be.getLevel(), be.getBlockPos(), be.getBlockState(), be, side);
                return handler == null ? null : fluidStorage(handler);
            }, type);
        } else if (capability == Capabilities.Energy.BLOCK) {
            EnergyStorage.SIDED.registerForBlockEntity((be, side) -> {
                EnergyHandler handler = query(Capabilities.Energy.BLOCK, be.getLevel(), be.getBlockPos(), be.getBlockState(), be, side);
                return handler == null ? null : energyStorage(handler);
            }, type);
        }
    }

    public static void onItemRegistered(ItemCapability<?, ?> capability, ItemLike... items) {
        if (capability == Capabilities.Fluid.ITEM) {
            FluidStorage.ITEM.registerForItems((stack, context) -> {
                ResourceHandler<FluidResource> handler = queryItem(Capabilities.Fluid.ITEM, stack, context);
                return handler == null ? null : fluidStorage(handler);
            }, items);
        } else if (capability == Capabilities.Energy.ITEM) {
            EnergyStorage.ITEM.registerForItems((stack, context) -> {
                EnergyHandler handler = queryItem(Capabilities.Energy.ITEM, stack, context);
                return handler == null ? null : energyStorage(handler);
            }, items);
        }
    }

    public static void onEntityRegistered(EntityCapability<?, ?> capability, EntityType<?> type) {
    }
}
