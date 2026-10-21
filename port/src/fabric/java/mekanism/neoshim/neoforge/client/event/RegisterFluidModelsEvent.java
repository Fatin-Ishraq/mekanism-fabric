package mekanism.neoshim.neoforge.client.event;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.world.level.material.Fluid;

/** Fired on the mod bus to collect fluid models; the client bridge registers them with Fabric's {@code FluidRenderingRegistry}. */
public final class RegisterFluidModelsEvent extends Event implements IModBusEvent {

    private final Map<Fluid, FluidModel.Unbaked> models = new LinkedHashMap<>();

    public void register(FluidModel.Unbaked model, Supplier<? extends Fluid> fluid) {
        models.put(fluid.get(), model);
    }

    public void register(FluidModel.Unbaked model, Fluid fluid) {
        models.put(fluid, model);
    }

    public void register(FluidModel.Unbaked model, Supplier<? extends Fluid> still, Supplier<? extends Fluid> flowing) {
        register(model, still.get());
        register(model, flowing.get());
    }

    public void register(FluidModel.Unbaked model, Fluid still, Fluid flowing) {
        register(model, still);
        register(model, flowing);
    }

    public Map<Fluid, FluidModel.Unbaked> getModels() {
        return models;
    }
}
