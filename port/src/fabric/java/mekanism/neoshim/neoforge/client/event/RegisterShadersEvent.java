package mekanism.neoshim.neoforge.client.event;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.util.function.Consumer;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.Identifier;

/**
 * Fired from Fabric's CoreShaderRegistrationCallback. NeoForge's variant takes a pre-built ShaderInstance; on Fabric the shader is created
 * by Fabric from its id, so callers use {@link #registerShader(Identifier, VertexFormat, Consumer)} (see the //FABRIC edit in
 * MekanismShaders).
 */
public class RegisterShadersEvent extends Event implements IModBusEvent {

    private final CoreShaderRegistrationCallback.RegistrationContext context;

    public RegisterShadersEvent(CoreShaderRegistrationCallback.RegistrationContext context) {
        this.context = context;
    }

    public void registerShader(Identifier id, VertexFormat vertexFormat, Consumer<ShaderInstance> onLoaded) throws IOException {
        context.register(id, vertexFormat, onLoaded);
    }
}
