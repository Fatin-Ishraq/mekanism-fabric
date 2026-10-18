package mekanism.fabric.client;

import mekanism.client.MekanismClient;
import mekanism.fabric.EventBusSubscribers;
import mekanism.fabric.MekanismFabric;
import mekanism.fabric.client.bridge.ClientBridge;
import net.fabricmc.api.ClientModInitializer;

/** Fabric client entrypoint: constructs Mekanism's client mod class and drives the client-side mod-bus events. */
public class MekanismFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        new MekanismClient(MekanismFabric.container());
        EventBusSubscribers.registerClient(MekanismFabric.container().getEventBus());
        ClientBridge.init(MekanismFabric.container());
    }
}
