package mekanism.neoshim.neoforge.common.extensions;

import mekanism.neoshim.neoforge.model.data.ModelData;
import net.minecraft.network.Connection;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;

/** Injected into {@link BlockEntity}. Lifecycle hooks are called by BlockEntityHooks (Fabric bridge), see NOTES.md "Hook status". */
public interface IBlockEntityExtension {

    private BlockEntity self() {
        return (BlockEntity) this;
    }

    /** Called on the client when a block entity data packet arrives (NeoForge default: load it). */
    default void onDataPacket(Connection net, ValueInput input) {
        self().loadWithComponents(input);
    }

    /** Called on the client with the chunk's initial block entity tag. */
    default void handleUpdateTag(ValueInput input) {
        self().loadWithComponents(input);
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

    default boolean hasCustomOutlineRendering(net.minecraft.world.entity.player.Player player) {
        return false;
    }

    default void applyStructureRotation(Mirror mirror, Rotation rotation) {
    }
}
