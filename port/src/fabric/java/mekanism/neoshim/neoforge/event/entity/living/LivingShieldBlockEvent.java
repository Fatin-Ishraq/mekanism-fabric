package mekanism.neoshim.neoforge.event.entity.living;

import mekanism.neoshim.bus.api.ICancellableEvent;
import mekanism.neoshim.neoforge.common.damagesource.DamageContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class LivingShieldBlockEvent extends LivingEvent implements ICancellableEvent {

    private final DamageContainer container;
    private final float originalBlockedDamage;
    private final boolean originalBlocked;
    private boolean blocked;
    private float dmgBlocked;
    private int shieldDamage = -1;

    public LivingShieldBlockEvent(LivingEntity blocker, DamageContainer container, float originalBlockedDamage, boolean originalBlockedState) {
        super(blocker);
        this.container = container;
        this.originalBlockedDamage = originalBlockedDamage;
        this.originalBlocked = originalBlockedState;
        this.blocked = originalBlockedState;
        this.dmgBlocked = originalBlockedDamage;
    }

    public DamageContainer getDamageContainer() {
        return container;
    }

    public DamageSource getDamageSource() {
        return container.getSource();
    }

    public float getOriginalBlockedDamage() {
        return originalBlockedDamage;
    }

    public float getBlockedDamage() {
        return dmgBlocked;
    }

    public int shieldDamage() {
        return blocked ? (shieldDamage >= 0 ? shieldDamage : Math.round(dmgBlocked)) : 0;
    }

    public void setBlockedDamage(float blocked) {
        this.dmgBlocked = Math.clamp(blocked, 0, container.getNewDamage());
    }

    public void setShieldDamage(int damage) {
        this.shieldDamage = damage;
    }

    public boolean getOriginalBlock() {
        return originalBlocked;
    }

    public boolean getBlocked() {
        return blocked;
    }

    public void setBlocked(boolean isBlocked) {
        this.blocked = isBlocked;
    }
}
