package mekanism.neoshim.neoforge.common.extensions;

import mekanism.neoshim.neoforge.model.data.ModelData;
import net.minecraft.network.Connection;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;

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

    default void onDataPacket(Connection net, ValueInput input) {
        handleUpdateTag(input);
    }

    default void handleUpdateTag(ValueInput input) {
        self().loadWithComponents(input);
    }

    default boolean hasCustomOutlineRendering(net.minecraft.world.entity.player.Player player) {
        return false;
    }
}
