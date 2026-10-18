package mekanism.fabric.mixin.client;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** NeoForge routes block entity update packets through {@code BlockEntity#onDataPacket}; Mekanism relies on it for its reduced update tags. */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    //Lambda inside handleBlockEntityData (synthetic name is stable: intermediary in both dev and production)
    @Redirect(method = "method_38542", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;loadWithComponents(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V"))
    private void mekanism$onDataPacket(BlockEntity blockEntity, CompoundTag tag, HolderLookup.Provider provider, ClientboundBlockEntityDataPacket packet, BlockEntity sameBlockEntity) {
        blockEntity.onDataPacket(((ClientPacketListener) (Object) this).getConnection(), packet, provider);
    }
}
