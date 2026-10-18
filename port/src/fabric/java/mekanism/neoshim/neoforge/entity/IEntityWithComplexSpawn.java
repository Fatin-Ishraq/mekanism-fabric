package mekanism.neoshim.neoforge.entity;

import net.minecraft.network.RegistryFriendlyByteBuf;

/** Extra spawn data. Sent by the port's spawn packet hook (TODO phase 5: wire via Fabric entity spawn packet mixin). */
public interface IEntityWithComplexSpawn {

    void writeSpawnData(RegistryFriendlyByteBuf buffer);

    void readSpawnData(RegistryFriendlyByteBuf additionalData);
}
