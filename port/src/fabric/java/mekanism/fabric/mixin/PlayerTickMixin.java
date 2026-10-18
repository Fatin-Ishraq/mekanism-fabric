package mekanism.fabric.mixin;

import mekanism.neoshim.neoforge.common.NeoForge;
import mekanism.neoshim.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's PlayerTickEvent.Pre/Post (both logical sides). */
@Mixin(Player.class)
public abstract class PlayerTickMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void mekanism$preTick(CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Pre((Player) (Object) this));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void mekanism$postTick(CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post((Player) (Object) this));
    }
}
