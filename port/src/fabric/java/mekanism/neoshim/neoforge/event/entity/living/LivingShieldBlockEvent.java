package mekanism.neoshim.neoforge.event.entity.living;

import mekanism.neoshim.bus.api.ICancellableEvent;
import mekanism.neoshim.neoforge.common.damagesource.DamageContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class LivingShieldBlockEvent extends LivingEvent implements ICancellableEvent {

    private final DamageContainer container;
    private final boolean originalBlocked;
    private boolean blocked;
    private float dmgBlocked;
    private float shieldDamage = -1;

    public LivingShieldBlockEvent(LivingEntity blocker, DamageContainer container, boolean originalBlockedState) {
        super(blocker);
        this.container = container;
        this.originalBlocked = originalBlockedState;
        this.blocked = originalBlockedState;
        this.dmgBlocked = container.getNewDamage();
    }

    public DamageContainer getDamageContainer() {
        return container;
    }

    public DamageSource getDamageSource() {
        return container.getSource();
    }

    public float getOriginalBlockedDamage() {
        return container.getNewDamage();
    }

    public float getBlockedDamage() {
        return dmgBlocked;
    }

    public float shieldDamage() {
        return blocked ? (shieldDamage >= 0 ? shieldDamage : dmgBlocked) : 0;
    }

    public void setBlockedDamage(float blocked) {
        this.dmgBlocked = Math.clamp(blocked, 0, container.getNewDamage());
    }

    public void setShieldDamage(float damage) {
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
