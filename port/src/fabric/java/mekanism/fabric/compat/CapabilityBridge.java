package mekanism.fabric.compat;

import mekanism.fabric.compat.transfer.EnergyStorageAdapters;
import mekanism.fabric.compat.transfer.FluidHandlerStorage;
import mekanism.fabric.compat.transfer.ItemHandlerStorage;
import mekanism.fabric.compat.transfer.StorageFluidHandler;
import mekanism.fabric.compat.transfer.StorageItemHandler;
import mekanism.neoshim.neoforge.capabilities.BlockCapability;
import mekanism.neoshim.neoforge.capabilities.Capabilities;
import mekanism.neoshim.neoforge.capabilities.EntityCapability;
import mekanism.neoshim.neoforge.capabilities.ItemCapability;
import mekanism.neoshim.neoforge.energy.IEnergyStorage;
import mekanism.neoshim.neoforge.fluids.capability.IFluidHandler;
import mekanism.neoshim.neoforge.items.IItemHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

/**
 * Interop between NeoForge-style capabilities and Fabric's Transfer API / Team Reborn Energy (NOTES.md D4):
 * <ul>
 *     <li>Export: whenever a provider for the standard item/fluid/energy block capability is registered, the same block (entity) also gets a
 *     Fabric {@link ItemStorage#SIDED}/{@link FluidStorage#SIDED}/{@link EnergyStorage#SIDED} provider wrapping it.</li>
 *     <li>Import: the standard block capabilities get fallback providers wrapping any foreign Fabric storage (vanilla containers included),
 *     unwrapping our own adapters to avoid chains.</li>
 * </ul>
 * Item-level (ContainerItemContext) interop is not bridged yet (NOTES.md "Stubbed / disabled").
 */
public final class CapabilityBridge {

    private static boolean fallbacksRegistered;
    /** Set while an export provider queries the NeoForge-side capability, so the import fallback cannot bounce back to Fabric (loop). */
    private static final ThreadLocal<Boolean> EXPORTING = ThreadLocal.withInitial(() -> false);

    private CapabilityBridge() {
    }

    /** Registers the import fallbacks; called once by the bootstrap before RegisterCapabilitiesEvent. */
    public static void registerFallbacks() {
        if (fallbacksRegistered) {
            return;
        }
        fallbacksRegistered = true;
        Capabilities.ItemHandler.BLOCK.lookup().registerFallback((level, pos, state, be, side) -> {
            if (EXPORTING.get()) {
                return null;
            }
            Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, state, be, side);
            if (storage == null) {
                return null;
            }
            return storage instanceof ItemHandlerStorage ours ? ours.handler() : new StorageItemHandler(storage);
        });
        Capabilities.FluidHandler.BLOCK.lookup().registerFallback((level, pos, state, be, side) -> {
            if (EXPORTING.get()) {
                return null;
            }
            Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, pos, state, be, side);
            if (storage == null) {
                return null;
            }
            return storage instanceof FluidHandlerStorage ours ? ours.handler() : new StorageFluidHandler(storage);
        });
        Capabilities.EnergyStorage.BLOCK.lookup().registerFallback((level, pos, state, be, side) -> {
            if (EXPORTING.get()) {
                return null;
            }
            EnergyStorage storage = EnergyStorage.SIDED.find(level, pos, state, be, side);
            if (storage == null) {
                return null;
            }
            return storage instanceof EnergyStorageAdapters.ToTeamReborn(IEnergyStorage ours) ? ours : new EnergyStorageAdapters.FromTeamReborn(storage);
        });
    }

    @Nullable
    private static <T> T query(BlockCapability<T, @Nullable Direction> capability, Level level, BlockPos pos, BlockState state, @Nullable BlockEntity be,
          @Nullable Direction side) {
        boolean previous = EXPORTING.get();
        EXPORTING.set(true);
        try {
            return capability.getCapability(level, pos, state, be, side);
        } finally {
            EXPORTING.set(previous);
        }
    }

    @SuppressWarnings("unchecked")
    public static void onBlockRegistered(BlockCapability<?, ?> capability, Block... blocks) {
        if (capability == Capabilities.ItemHandler.BLOCK) {
            ItemStorage.SIDED.registerForBlocks((level, pos, state, be, side) -> {
                IItemHandler handler = query(Capabilities.ItemHandler.BLOCK, level, pos, state, be, side);
                return handler == null ? null : new ItemHandlerStorage(handler);
            }, blocks);
        } else if (capability == Capabilities.FluidHandler.BLOCK) {
            FluidStorage.SIDED.registerForBlocks((level, pos, state, be, side) -> {
                IFluidHandler handler = query(Capabilities.FluidHandler.BLOCK, level, pos, state, be, side);
                return handler == null ? null : new FluidHandlerStorage(handler);
            }, blocks);
        } else if (capability == Capabilities.EnergyStorage.BLOCK) {
            EnergyStorage.SIDED.registerForBlocks((level, pos, state, be, side) -> {
                IEnergyStorage storage = query(Capabilities.EnergyStorage.BLOCK, level, pos, state, be, side);
                return storage == null ? null : new EnergyStorageAdapters.ToTeamReborn(storage);
            }, blocks);
        }
    }

    public static void onBlockEntityRegistered(BlockCapability<?, ?> capability, BlockEntityType<?> type) {
        if (capability == Capabilities.ItemHandler.BLOCK) {
            ItemStorage.SIDED.registerForBlockEntity((be, side) -> {
                IItemHandler handler = query(Capabilities.ItemHandler.BLOCK, be.getLevel(), be.getBlockPos(), be.getBlockState(), be, side);
                return handler == null ? null : new ItemHandlerStorage(handler);
            }, type);
        } else if (capability == Capabilities.FluidHandler.BLOCK) {
            FluidStorage.SIDED.registerForBlockEntity((be, side) -> {
                IFluidHandler handler = query(Capabilities.FluidHandler.BLOCK, be.getLevel(), be.getBlockPos(), be.getBlockState(), be, side);
                return handler == null ? null : new FluidHandlerStorage(handler);
            }, type);
        } else if (capability == Capabilities.EnergyStorage.BLOCK) {
            EnergyStorage.SIDED.registerForBlockEntity((be, side) -> {
                IEnergyStorage storage = query(Capabilities.EnergyStorage.BLOCK, be.getLevel(), be.getBlockPos(), be.getBlockState(), be, side);
                return storage == null ? null : new EnergyStorageAdapters.ToTeamReborn(storage);
            }, type);
        }
    }

    public static void onItemRegistered(ItemCapability<?, ?> capability, ItemLike... items) {
        //Item-level interop (ContainerItemContext) is TODO
    }

    public static void onEntityRegistered(EntityCapability<?, ?> capability, EntityType<?> type) {
    }
}
