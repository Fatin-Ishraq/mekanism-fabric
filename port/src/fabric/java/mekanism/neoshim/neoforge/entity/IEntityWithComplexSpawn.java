package mekanism.neoshim.neoforge.entity;

import net.minecraft.network.RegistryFriendlyByteBuf;

/** Extra spawn data, sent right after the add-entity packet (mekanism.fabric.entity.ComplexSpawns / ServerEntityMixin). */
public interface IEntityWithComplexSpawn {

    void writeSpawnData(RegistryFriendlyByteBuf buffer);

    void readSpawnData(RegistryFriendlyByteBuf additionalData);
}
