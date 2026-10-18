package mekanism.fabric.mixin;

import java.util.function.Consumer;
import mekanism.fabric.entity.ComplexSpawns;
import mekanism.neoshim.neoforge.entity.IEntityWithComplexSpawn;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Sends {@link IEntityWithComplexSpawn} data immediately after the add-entity packet. */
@Mixin(ServerEntity.class)
public abstract class ServerEntityMixin {

    @Shadow
    @Final
    private Entity entity;

    @Inject(method = "sendPairingData", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 0, shift = At.Shift.AFTER))
    private void mekanism$complexSpawn(ServerPlayer player, Consumer<Packet<ClientGamePacketListener>> consumer, CallbackInfo ci) {
        if (entity instanceof IEntityWithComplexSpawn complexSpawn) {
            consumer.accept(ComplexSpawns.createPacket(entity, complexSpawn));
        }
    }
}
