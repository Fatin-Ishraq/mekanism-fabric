package mekanism.neoshim.neoforge.common.extensions;

import java.util.Set;
import mekanism.neoshim.neoforge.common.EffectCure;
import mekanism.neoshim.neoforge.common.EffectCures;

/** Injected into {@link net.minecraft.world.effect.MobEffectInstance}. Fabric has no cures: every effect is curable by milk. */
public interface IMobEffectInstanceExtension {

    default Set<EffectCure> getCures() {
        return EffectCures.DEFAULT_CURES;
    }
}
