package mekanism.fabric;

import mekanism.neoshim.bus.api.IEventBus;
import mekanism.neoshim.bus.EventBusImpl;
import mekanism.neoshim.fml.event.IModBusEvent;
import mekanism.neoshim.neoforge.common.NeoForge;

/**
 * Explicit replacement for FML's {@code @EventBusSubscriber} classpath scan. Each listed class is registered on both buses; each of its
 * {@code @SubscribeEvent} methods lands on the mod bus if its event type is an {@link IModBusEvent}, otherwise on the game bus (NeoForge's
 * automatic bus detection). Keep in sync with: grep -rl "@EventBusSubscriber" src/main.
 */
public final class EventBusSubscribers {

    private static final String[] COMMON = {
          "mekanism.common.lib.multiblock.MultiblockManager",
          "mekanism.common.lib.radiation.MeltdownLevelData",
          "mekanism.common.lib.radiation.PlayerExposure",
          "mekanism.common.lib.transmitter.TransmitterNetworkRegistry",
    };
    private static final String[] CLIENT = {
          "mekanism.client.ClientRegistration",
          "mekanism.client.render.MekanismRenderer",
          "mekanism.client.render.MekanismShaders",
          "mekanism.client.sound.SoundHandler",
          "mekanism.common.base.holiday.ClientHolidayInfo",
    };

    private EventBusSubscribers() {
    }

    public static void registerCommon(IEventBus modBus) {
        register(modBus, COMMON);
    }

    public static void registerClient(IEventBus modBus) {
        register(modBus, CLIENT);
    }

    public static void register(IEventBus modBus, String[] classNames) {
        for (String className : classNames) {
            Class<?> clazz;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Missing event subscriber class " + className, e);
            }
            ((EventBusImpl) modBus).registerFiltered(clazz, IModBusEvent.class::isAssignableFrom);
            ((EventBusImpl) NeoForge.EVENT_BUS).registerFiltered(clazz, type -> !IModBusEvent.class.isAssignableFrom(type));
        }
    }
}
