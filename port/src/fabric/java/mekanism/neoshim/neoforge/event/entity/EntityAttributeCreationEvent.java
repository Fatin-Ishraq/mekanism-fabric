package mekanism.neoshim.neoforge.event.entity;

import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

public class EntityAttributeCreationEvent extends Event implements IModBusEvent {

    public void put(EntityType<? extends LivingEntity> entity, AttributeSupplier map) {
        FabricDefaultAttributeRegistry.register(entity, map);
    }
}
