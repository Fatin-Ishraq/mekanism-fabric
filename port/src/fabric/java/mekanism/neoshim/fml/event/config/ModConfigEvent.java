package mekanism.neoshim.fml.event.config;

import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.neoforged.fml.config.ModConfig;

/** Bridged from Forge Config API Port's {@code NeoForgeModConfigEvents} by the bootstrap (NOTES.md D7). */
public class ModConfigEvent extends Event implements IModBusEvent {

    private final ModConfig config;

    protected ModConfigEvent(ModConfig config) {
        this.config = config;
    }

    public ModConfig getConfig() {
        return config;
    }

    public static class Loading extends ModConfigEvent {

        public Loading(ModConfig config) {
            super(config);
        }
    }

    public static class Reloading extends ModConfigEvent {

        public Reloading(ModConfig config) {
            super(config);
        }
    }

    public static class Unloading extends ModConfigEvent {

        public Unloading(ModConfig config) {
            super(config);
        }
    }
}
