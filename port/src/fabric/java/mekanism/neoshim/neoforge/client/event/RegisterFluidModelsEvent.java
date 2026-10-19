package mekanism.neoshim.neoforge.client.event;

import java.util.function.Supplier;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.world.level.material.Fluid;

/** Fired once during client init; models are registered with Fabric's {@link FluidRenderingRegistry}, which bakes them with vanilla's fluid models. */
public final class RegisterFluidModelsEvent extends Event implements IModBusEvent {

    public void register(FluidModel.Unbaked model, Supplier<? extends Fluid> fluid) {
        register(model, fluid.get());
    }

    public void register(FluidModel.Unbaked model, Fluid fluid) {
        FluidRenderingRegistry.register(fluid, model);
    }

    public void register(FluidModel.Unbaked model, Supplier<? extends Fluid> still, Supplier<? extends Fluid> flowing) {
        register(model, still.get(), flowing.get());
    }

    public void register(FluidModel.Unbaked model, Fluid still, Fluid flowing) {
        FluidRenderingRegistry.register(still, flowing, model);
    }
}
