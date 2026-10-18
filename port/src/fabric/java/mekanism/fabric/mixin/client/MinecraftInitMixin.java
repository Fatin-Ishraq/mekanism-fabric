package mekanism.fabric.mixin.client;

import mekanism.fabric.client.bridge.ClientBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fires the late client mod-bus events (RegisterClientReloadListenersEvent, color handlers) at the point NeoForge does: inside the Minecraft
 * constructor once the GL context, texture manager and color registries exist, right before the initial resource reload is created.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftInitMixin {

    @Shadow
    @Final
    private ReloadableResourceManager resourceManager;

    @Inject(method = "<init>", at = @At(value = "INVOKE",
          target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;createReload(Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Ljava/util/List;)Lnet/minecraft/server/packs/resources/ReloadInstance;"))
    private void mekanism$beforeInitialReload(GameConfig gameConfig, CallbackInfo ci) {
        ClientBridge.onBeforeInitialReload((Minecraft) (Object) this, resourceManager);
    }
}
