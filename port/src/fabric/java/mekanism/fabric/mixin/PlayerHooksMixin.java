package mekanism.fabric.mixin;

import mekanism.fabric.NeoAttributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code neoforge:creative_flight}: NeoForge patches every {@code abilities.mayfly} check to also consult the attribute. Here the server grants (and later revokes)
 * {@code mayfly} itself while the attribute is positive, and the vanilla abilities packet syncs it to the client. Whether flight was granted by the attribute is saved
 * with the player so it is revoked correctly after a relog.
 */
@Mixin(Player.class)
public abstract class PlayerHooksMixin {

    @Unique
    private static final String GRANTED_FLIGHT_KEY = "mekanism_fabric:attribute_flight";

    @Unique
    private boolean mekanism$attributeFlight;

    @Inject(method = "createAttributes", at = @At("RETURN"))
    private static void mekanism$addAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.getReturnValue().add(NeoAttributes.CREATIVE_FLIGHT);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void mekanism$creativeFlight(CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (self.level().isClientSide) {
            return;
        }
        Abilities abilities = self.getAbilities();
        boolean attributeAllows = self.getAttributeValue(NeoAttributes.CREATIVE_FLIGHT) > 0;
        if (attributeAllows) {
            if (!abilities.mayfly) {
                abilities.mayfly = true;
                mekanism$attributeFlight = true;
                self.onUpdateAbilities();
            }
        } else if (mekanism$attributeFlight) {
            mekanism$attributeFlight = false;
            if (!self.isCreative() && !self.isSpectator()) {
                abilities.mayfly = false;
                abilities.flying = false;
                self.onUpdateAbilities();
            }
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void mekanism$saveFlight(net.minecraft.world.level.storage.ValueOutput output, CallbackInfo ci) {
        if (mekanism$attributeFlight) {
            output.putBoolean(GRANTED_FLIGHT_KEY, true);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void mekanism$loadFlight(net.minecraft.world.level.storage.ValueInput input, CallbackInfo ci) {
        mekanism$attributeFlight = input.getBooleanOr(GRANTED_FLIGHT_KEY, false);
    }
}
