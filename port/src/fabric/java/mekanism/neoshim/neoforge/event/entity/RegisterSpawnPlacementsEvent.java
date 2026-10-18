package mekanism.neoshim.neoforge.event.entity;

import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

public class RegisterSpawnPlacementsEvent extends Event implements IModBusEvent {

    public enum Operation {
        REPLACE,
        AND,
        OR
    }

    public <T extends Entity> void register(EntityType<T> entityType, SpawnPlacementType placementType, Heightmap.Types heightmap,
          SpawnPlacements.SpawnPredicate<T> predicate, Operation operation) {
        registerMob(entityType, placementType, heightmap, predicate);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerMob(EntityType entityType, SpawnPlacementType placementType, Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate predicate) {
        SpawnPlacements.register((EntityType<Mob>) entityType, placementType, heightmap, predicate);
    }
}
