package mekanism.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mekanism.fabric.NeoAttributes;
import mekanism.neoshim.neoforge.common.NeoForge;
import mekanism.neoshim.neoforge.common.damagesource.DamageContainer;
import mekanism.neoshim.neoforge.event.entity.living.LivingEvent;
import mekanism.neoshim.neoforge.event.entity.living.LivingFallEvent;
import mekanism.neoshim.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * NeoForge's living entity hooks: LivingIncomingDamageEvent, LivingFallEvent, LivingJumpEvent and the {@code neoforge:swim_speed} attribute.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityHooksMixin {

    @WrapMethod(method = "hurt")
    private boolean mekanism$incomingDamage(DamageSource source, float amount, Operation<Boolean> original) {
        LivingEntity self = (LivingEntity) (Object) this;
        //Same preconditions NeoForge checks before firing the event (invulnerable, client side, already dead)
        if (!self.level().isClientSide && !self.isInvulnerableTo(source) && !self.isDeadOrDying()) {
            DamageContainer container = new DamageContainer(source, Math.max(amount, 0));
            if (NeoForge.EVENT_BUS.post(new LivingIncomingDamageEvent(self, container)).isCanceled()) {
                return false;
            }
            amount = container.getNewDamage();
        }
        return original.call(source, amount);
    }

    @WrapMethod(method = "causeFallDamage")
    private boolean mekanism$fall(float fallDistance, float multiplier, DamageSource source, Operation<Boolean> original) {
        LivingFallEvent event = NeoForge.EVENT_BUS.post(new LivingFallEvent((LivingEntity) (Object) this, fallDistance, multiplier));
        if (event.isCanceled()) {
            return false;
        }
        return original.call(event.getDistance(), event.getDamageMultiplier(), source);
    }

    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    private void mekanism$jump(CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new LivingEvent.LivingJumpEvent((LivingEntity) (Object) this));
    }

    //The first moveRelative in travel is the in-water branch
    @ModifyArg(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V", ordinal = 0), index = 0)
    private float mekanism$swimSpeed(float speed) {
        return speed * (float) ((LivingEntity) (Object) this).getAttributeValue(NeoAttributes.SWIM_SPEED);
    }

    @Inject(method = "createLivingAttributes", at = @At("RETURN"))
    private static void mekanism$addAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.getReturnValue().add(NeoAttributes.SWIM_SPEED);
    }
}
