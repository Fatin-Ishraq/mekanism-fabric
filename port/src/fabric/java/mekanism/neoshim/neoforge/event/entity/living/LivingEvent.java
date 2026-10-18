package mekanism.neoshim.neoforge.event.entity.living;

import mekanism.neoshim.neoforge.event.entity.EntityEvent;
import net.minecraft.world.entity.LivingEntity;

public abstract class LivingEvent extends EntityEvent {

    private final LivingEntity livingEntity;

    protected LivingEvent(LivingEntity entity) {
        super(entity);
        this.livingEntity = entity;
    }

    @Override
    public LivingEntity getEntity() {
        return livingEntity;
    }

    public static class LivingJumpEvent extends LivingEvent {

        public LivingJumpEvent(LivingEntity e) {
            super(e);
        }
    }

    public static class LivingVisibilityEvent extends LivingEvent {

        private double visibilityModifier;

        public LivingVisibilityEvent(LivingEntity livingEntity, double originalMultiplier) {
            super(livingEntity);
            this.visibilityModifier = originalMultiplier;
        }

        public void modifyVisibility(double mod) {
            visibilityModifier *= mod;
        }

        public double getVisibilityModifier() {
            return visibilityModifier;
        }
    }
}
