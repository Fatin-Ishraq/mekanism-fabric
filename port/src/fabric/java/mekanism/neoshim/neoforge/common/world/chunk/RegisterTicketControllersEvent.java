package mekanism.neoshim.neoforge.common.world.chunk;

import java.util.ArrayList;
import java.util.List;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;

public class RegisterTicketControllersEvent extends Event implements IModBusEvent {

    private final List<TicketController> controllers = new ArrayList<>();

    public void register(TicketController controller) {
        controllers.add(controller);
    }

    public List<TicketController> getControllers() {
        return controllers;
    }
}
