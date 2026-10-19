package mekanism.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mekanism.neoshim.neoforge.common.NeoForge;
import mekanism.neoshim.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

/**
 * NeoForge's FinalizeSpawnEvent. NeoForge fires it around each finalizeSpawn call site; wrapping the base Mob implementation (which subclass overrides call
 * first) covers every spawn path. Cancelling skips the base implementation.
 */
@Mixin(Mob.class)
public abstract class MobFinalizeSpawnMixin {

    @WrapMethod(method = "finalizeSpawn")
    @Nullable
    private SpawnGroupData mekanism$finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnData,
          Operation<SpawnGroupData> original) {
        FinalizeSpawnEvent event = NeoForge.EVENT_BUS.post(new FinalizeSpawnEvent((Mob) (Object) this, level, difficulty, spawnType, spawnData));
        if (event.isCanceled()) {
            return event.getSpawnData();
        }
        return original.call(level, event.getDifficulty(), spawnType, event.getSpawnData());
    }
}
