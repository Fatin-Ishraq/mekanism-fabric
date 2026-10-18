package mekanism.neoshim.neoforge.common.damagesource;

import net.minecraft.world.damagesource.DamageSource;

public class DamageContainer {

    public enum Reduction {
        ARMOR,
        ENCHANTMENTS,
        MOB_EFFECTS,
        ABSORPTION
    }

    private final DamageSource source;
    private final float originalDamage;
    private float newDamage;
    private float blockedDamage;
    private int invulnerabilityTicks = 20;

    public DamageContainer(DamageSource source, float originalDamage) {
        this.source = source;
        this.originalDamage = originalDamage;
        this.newDamage = originalDamage;
    }

    public DamageSource getSource() {
        return source;
    }

    public float getOriginalDamage() {
        return originalDamage;
    }

    public float getNewDamage() {
        return newDamage;
    }

    public void setNewDamage(float damage) {
        this.newDamage = damage;
    }

    public float getBlockedDamage() {
        return blockedDamage;
    }

    public void setBlockedDamage(float blockedDamage) {
        this.blockedDamage = blockedDamage;
    }

    public int getPostAttackInvulnerabilityTicks() {
        return invulnerabilityTicks;
    }

    public void setPostAttackInvulnerabilityTicks(int ticks) {
        this.invulnerabilityTicks = ticks;
    }
}
