package mekanism.common.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import mekanism.neoshim.neoforge.capabilities.BlockCapability;
import mekanism.neoshim.neoforge.capabilities.EntityCapability;
import mekanism.neoshim.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.Nullable;

public record MultiTypeCapability<HANDLER>(BlockCapability<HANDLER, @Nullable Direction> block,
                                           ItemCapability<HANDLER, Void> item,
                                           EntityCapability<HANDLER, ?> entity) implements IMultiTypeCapability<HANDLER, HANDLER> {

    public MultiTypeCapability(ResourceLocation name, Class<HANDLER> handlerClass) {
        this(
              BlockCapability.createSided(name, handlerClass),
              ItemCapability.createVoid(name, handlerClass),
              EntityCapability.createVoid(name, handlerClass)
        );
    }
}