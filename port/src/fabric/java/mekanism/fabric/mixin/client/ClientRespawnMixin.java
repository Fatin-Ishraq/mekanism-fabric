package mekanism.fabric.mixin.client;

import mekanism.neoshim.neoforge.client.event.ClientPlayerNetworkEvent;
import mekanism.neoshim.neoforge.common.NeoForge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's ClientPlayerNetworkEvent.Clone, fired once the respawn packet replaced the local player. */
@Mixin(ClientPacketListener.class)
public abstract class ClientRespawnMixin {

    @Unique
    private LocalPlayer mekanism$oldPlayer;

    @Inject(method = "handleRespawn", at = @At("HEAD"))
    private void mekanism$captureOldPlayer(ClientboundRespawnPacket packet, CallbackInfo ci) {
        mekanism$oldPlayer = Minecraft.getInstance().player;
    }

    @Inject(method = "handleRespawn", at = @At("TAIL"))
    private void mekanism$clone(ClientboundRespawnPacket packet, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer oldPlayer = mekanism$oldPlayer;
        mekanism$oldPlayer = null;
        if (oldPlayer != null && minecraft.player != null && minecraft.player != oldPlayer) {
            NeoForge.EVENT_BUS.post(new ClientPlayerNetworkEvent.Clone(minecraft.gameMode, oldPlayer, minecraft.player, ((ClientPacketListener) (Object) this).getConnection()));
        }
    }
}
