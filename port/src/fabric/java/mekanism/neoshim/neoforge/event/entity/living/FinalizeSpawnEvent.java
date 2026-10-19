package mekanism.neoshim.neoforge.event.entity.living;

import mekanism.neoshim.bus.api.ICancellableEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

public class FinalizeSpawnEvent extends LivingEvent implements ICancellableEvent {

    private final ServerLevelAccessor level;
    private DifficultyInstance difficulty;
    private final EntitySpawnReason spawnType;
    @Nullable
    private SpawnGroupData spawnData;

    public FinalizeSpawnEvent(Mob entity, ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnData) {
        super(entity);
        this.level = level;
        this.difficulty = difficulty;
        this.spawnType = spawnType;
        this.spawnData = spawnData;
    }

    @Override
    public Mob getEntity() {
        return (Mob) super.getEntity();
    }

    public ServerLevelAccessor getLevel() {
        return level;
    }

    public DifficultyInstance getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyInstance difficulty) {
        this.difficulty = difficulty;
    }

    public EntitySpawnReason getSpawnType() {
        return spawnType;
    }

    @Nullable
    public SpawnGroupData getSpawnData() {
        return spawnData;
    }

    public void setSpawnData(@Nullable SpawnGroupData spawnData) {
        this.spawnData = spawnData;
    }
}
