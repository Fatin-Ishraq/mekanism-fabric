package mekanism.neoshim.neoforge.network.event;

import java.util.function.Consumer;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.network.ConfigurationTask;

/** Fired for each connection entering the configuration phase (bridged from Fabric's ServerConfigurationConnectionEvents.CONFIGURE). */
public class RegisterConfigurationTasksEvent extends Event implements IModBusEvent {

    private final ServerConfigurationPacketListener listener;
    private final Consumer<ConfigurationTask> taskAdder;

    public RegisterConfigurationTasksEvent(ServerConfigurationPacketListener listener, Consumer<ConfigurationTask> taskAdder) {
        this.listener = listener;
        this.taskAdder = taskAdder;
    }

    public void register(ConfigurationTask task) {
        taskAdder.accept(task);
    }

    public ServerConfigurationPacketListener getListener() {
        return listener;
    }
}
