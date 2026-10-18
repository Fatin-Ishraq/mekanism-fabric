package mekanism.neoshim.neoforge.registries.datamaps;

import mekanism.fabric.datamaps.DataMapLoader;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;

public class RegisterDataMapTypesEvent extends Event implements IModBusEvent {

    public <T, R> void register(DataMapType<R, T> type) {
        DataMapLoader.register(type);
    }
}
