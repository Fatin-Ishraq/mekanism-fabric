package mekanism.neoshim.neoforge.entity;

import net.minecraft.world.entity.Entity;

/** Multipart entity piece. Vanilla's ender dragon parts are matched separately by the port's teleporter check. */
public abstract class PartEntity<T extends Entity> extends Entity {

    private final T parent;

    protected PartEntity(T parent) {
        super(parent.getType(), parent.level());
        this.parent = parent;
    }

    public T getParent() {
        return parent;
    }
}
