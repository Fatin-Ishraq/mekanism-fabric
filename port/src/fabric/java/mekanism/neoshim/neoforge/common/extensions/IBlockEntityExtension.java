package mekanism.neoshim.neoforge.common.extensions;

import mekanism.neoshim.neoforge.client.model.data.ModelData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Injected into {@link BlockEntity}. Lifecycle hooks are called by BlockEntityHooks (Fabric bridge), see NOTES.md "Hook status". */
public interface IBlockEntityExtension {

    private BlockEntity self() {
        return (BlockEntity) this;
    }

    default void onLoad() {
    }

    default void onChunkUnloaded() {
    }

    default ModelData getModelData() {
        return ModelData.EMPTY;
    }

    default void requestModelDataUpdate() {
    }

    default void invalidateCapabilities() {
        if (self().getLevel() != null) {
            self().getLevel().invalidateCapabilities(self().getBlockPos());
        }
    }

    default void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        CompoundTag tag = pkt.getTag();
        if (!tag.isEmpty()) {
            self().loadWithComponents(tag, lookupProvider);
        }
    }

    default void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        self().loadWithComponents(tag, lookupProvider);
    }

    default boolean hasCustomOutlineRendering(net.minecraft.world.entity.player.Player player) {
        return false;
    }
}
