package mekanism.fabric.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.Map;
import mekanism.fabric.bridge.ModBusLifecycle;
import mekanism.neoshim.fml.ModContainer;
import mekanism.neoshim.neoforge.client.event.EntityRenderersEvent;
import net.minecraft.client.entity.ClientMannequin;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.PlayerModelType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's EntityRenderersEvent.AddLayers, fired after entity renderers are (re)created. */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Shadow
    private Map<EntityType<?>, EntityRenderer<?, ?>> renderers;
    @Shadow
    private Map<PlayerModelType, AvatarRenderer<AbstractClientPlayer>> playerRenderers;
    @Shadow
    private Map<PlayerModelType, AvatarRenderer<ClientMannequin>> mannequinRenderers;

    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void mekanism$addLayers(ResourceManager resourceManager, CallbackInfo ci, @Local EntityRendererProvider.Context context) {
        for (ModContainer mod : ModBusLifecycle.mods()) {
            mod.getEventBus().post(new EntityRenderersEvent.AddLayers(renderers, playerRenderers, mannequinRenderers, context));
        }
    }
}
