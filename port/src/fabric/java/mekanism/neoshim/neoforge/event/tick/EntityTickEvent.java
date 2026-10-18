package mekanism.neoshim.neoforge.event.tick;

import mekanism.neoshim.bus.api.ICancellableEvent;
import mekanism.neoshim.neoforge.event.entity.EntityEvent;
import net.minecraft.world.entity.Entity;

public abstract class EntityTickEvent extends EntityEvent {

    protected EntityTickEvent(Entity entity) {
        super(entity);
    }

    public static class Pre extends EntityTickEvent implements ICancellableEvent {

        public Pre(Entity entity) {
            super(entity);
        }
    }

    public static class Post extends EntityTickEvent {

        public Post(Entity entity) {
            super(entity);
        }
    }
}
