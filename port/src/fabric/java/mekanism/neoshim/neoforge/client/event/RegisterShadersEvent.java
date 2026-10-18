package mekanism.neoshim.neoforge.client.event;

import java.util.List;
import java.util.function.Consumer;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import com.mojang.datafixers.util.Pair;

public class RegisterShadersEvent extends Event implements IModBusEvent {

    private final ResourceProvider resourceProvider;
    private final List<Pair<ShaderInstance, Consumer<ShaderInstance>>> shaderList;

    public RegisterShadersEvent(ResourceProvider resourceProvider, List<Pair<ShaderInstance, Consumer<ShaderInstance>>> shaderList) {
        this.resourceProvider = resourceProvider;
        this.shaderList = shaderList;
    }

    public ResourceProvider getResourceProvider() {
        return resourceProvider;
    }

    public void registerShader(ShaderInstance shaderInstance, Consumer<ShaderInstance> onLoaded) {
        shaderList.add(Pair.of(shaderInstance, onLoaded));
    }
}
