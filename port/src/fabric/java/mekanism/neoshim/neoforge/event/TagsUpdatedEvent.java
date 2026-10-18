package mekanism.neoshim.neoforge.event;

import mekanism.neoshim.bus.api.Event;
import net.minecraft.core.RegistryAccess;

public class TagsUpdatedEvent extends Event {

    public enum UpdateCause {
        SERVER_DATA_LOAD,
        CLIENT_PACKET_RECEIVED
    }

    private final RegistryAccess registryAccess;
    private final UpdateCause updateCause;
    private final boolean integratedServer;

    public TagsUpdatedEvent(RegistryAccess registryAccess, boolean fromClientPacket, boolean isIntegratedServerConnection) {
        this.registryAccess = registryAccess;
        this.updateCause = fromClientPacket ? UpdateCause.CLIENT_PACKET_RECEIVED : UpdateCause.SERVER_DATA_LOAD;
        this.integratedServer = isIntegratedServerConnection;
    }

    public RegistryAccess getRegistryAccess() {
        return registryAccess;
    }

    public UpdateCause getUpdateCause() {
        return updateCause;
    }

    public boolean shouldUpdateStaticData() {
        return updateCause == UpdateCause.SERVER_DATA_LOAD || !integratedServer;
    }
}
