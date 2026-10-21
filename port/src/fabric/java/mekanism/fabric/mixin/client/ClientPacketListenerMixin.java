package mekanism.fabric.mixin.client;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** NeoForge routes block entity update packets through {@code BlockEntity#onDataPacket}; Mekanism relies on it for its reduced update tags. */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    //The load call lives in the ifPresent lambda of handleBlockEntityData
    @Redirect(method = "lambda$handleBlockEntityData$0",
          at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;loadWithComponents(Lnet/minecraft/world/level/storage/ValueInput;)V"))
    private void mekanism$onDataPacket(BlockEntity blockEntity, ValueInput input, ClientboundBlockEntityDataPacket packet, BlockEntity sameBlockEntity) {
        blockEntity.onDataPacket(((ClientPacketListener) (Object) this).getConnection(), input);
    }
}
