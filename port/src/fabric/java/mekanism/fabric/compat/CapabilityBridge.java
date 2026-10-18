package mekanism.fabric.compat;

import mekanism.neoshim.neoforge.capabilities.BlockCapability;
import mekanism.neoshim.neoforge.capabilities.EntityCapability;
import mekanism.neoshim.neoforge.capabilities.ItemCapability;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Interop between NeoForge-style capabilities and Fabric's Transfer API / Team Reborn Energy (NOTES.md D4).
 * TODO(phase 5): register Storage/EnergyStorage adapters for Mekanism providers and fallback providers wrapping foreign storages.
 */
public final class CapabilityBridge {

    private CapabilityBridge() {
    }

    public static void onBlockRegistered(BlockCapability<?, ?> capability, Block... blocks) {
    }

    public static void onBlockEntityRegistered(BlockCapability<?, ?> capability, BlockEntityType<?> type) {
    }

    public static void onItemRegistered(ItemCapability<?, ?> capability, ItemLike... items) {
    }

    public static void onEntityRegistered(EntityCapability<?, ?> capability, EntityType<?> type) {
    }
}
