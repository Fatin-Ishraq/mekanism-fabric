package mekanism.fabric.mixin.client;

import mekanism.neoshim.neoforge.client.event.RecipesUpdatedEvent;
import mekanism.neoshim.neoforge.common.NeoForge;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's RecipesUpdatedEvent (client recipe sync). */
@Mixin(ClientPacketListener.class)
public abstract class ClientRecipesMixin {

    @Inject(method = "handleUpdateRecipes", at = @At("TAIL"))
    private void mekanism$recipesUpdated(ClientboundUpdateRecipesPacket packet, CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new RecipesUpdatedEvent(((ClientPacketListener) (Object) this).getRecipeManager()));
    }
}
