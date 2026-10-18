package mekanism.fabric.mixin;

import mekanism.neoshim.neoforge.common.NeoForge;
import mekanism.neoshim.neoforge.event.tick.EntityTickEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's EntityTickEvent.Post on the logical server (Mekanism only listens server side). */
@Mixin(ServerLevel.class)
public abstract class ServerLevelEntityTickMixin {

    @Inject(method = "tickNonPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V", shift = At.Shift.AFTER))
    private void mekanism$postTick(Entity entity, CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new EntityTickEvent.Post(entity));
    }

    @Inject(method = "tickPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;rideTick()V", shift = At.Shift.AFTER))
    private void mekanism$postRideTick(Entity ridingEntity, Entity passengerEntity, CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new EntityTickEvent.Post(passengerEntity));
    }
}
