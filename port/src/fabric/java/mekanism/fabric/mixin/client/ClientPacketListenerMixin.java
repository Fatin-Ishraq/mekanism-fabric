package mekanism.fabric.mixin.client;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** NeoForge routes block entity update packets through {@code BlockEntity#onDataPacket}; Mekanism relies on it for its reduced update tags. */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    //Lambda inside handleBlockEntityData (26.1 is unobfuscated, so the synthetic name is the same in dev and production)
    @Redirect(method = "lambda$handleBlockEntityData$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;loadWithComponents(Lnet/minecraft/world/level/storage/ValueInput;)V"))
    private void mekanism$onDataPacket(BlockEntity blockEntity, ValueInput input) {
        blockEntity.onDataPacket(((ClientPacketListener) (Object) this).getConnection(), input);
    }
}
