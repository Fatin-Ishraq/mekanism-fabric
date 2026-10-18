package mekanism.fabric.mixin;

import mekanism.neoshim.neoforge.common.extensions.ILightningBoltExtension;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LightningBolt.class)
public abstract class LightningBoltMixin implements ILightningBoltExtension {

    @Unique
    private float mekanism$damage = 5.0F;

    @Override
    public void setDamage(float damage) {
        this.mekanism$damage = damage;
    }

    @Override
    public float getDamage() {
        return mekanism$damage;
    }
}
