package mekanism.neoshim.neoforge.event.entity;

import mekanism.neoshim.bus.api.ICancellableEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class EntityTeleportEvent extends EntityEvent implements ICancellableEvent {

    protected double targetX;
    protected double targetY;
    protected double targetZ;
    @Nullable
    protected final ServerLevel targetLevel;

    public EntityTeleportEvent(Entity entity, @Nullable ServerLevel targetLevel, double targetX, double targetY, double targetZ) {
        super(entity);
        this.targetLevel = targetLevel;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
    }

    @Nullable
    public ServerLevel getTargetLevel() {
        return targetLevel;
    }

    public double getTargetX() {
        return targetX;
    }

    public void setTargetX(double targetX) {
        this.targetX = targetX;
    }

    public double getTargetY() {
        return targetY;
    }

    public void setTargetY(double targetY) {
        this.targetY = targetY;
    }

    public double getTargetZ() {
        return targetZ;
    }

    public void setTargetZ(double targetZ) {
        this.targetZ = targetZ;
    }

    public Vec3 getTarget() {
        return new Vec3(this.targetX, this.targetY, this.targetZ);
    }

    public double getPrevX() {
        return getEntity().getX();
    }

    public double getPrevY() {
        return getEntity().getY();
    }

    public double getPrevZ() {
        return getEntity().getZ();
    }

    public Vec3 getPrev() {
        return getEntity().position();
    }
}
