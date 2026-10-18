package mekanism.neoshim.neoforge.common.extensions;

import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

/** Injected into {@link net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener}. */
public interface IServerConfigurationPacketListenerExtension {

    default void finishCurrentTask(ConfigurationTask.Type type) {
        ((ServerConfigurationPacketListenerImpl) this).finishCurrentTask(type);
    }
}
