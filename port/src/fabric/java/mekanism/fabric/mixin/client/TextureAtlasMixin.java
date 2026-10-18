package mekanism.fabric.mixin.client;

import mekanism.fabric.bridge.ModBusLifecycle;
import mekanism.neoshim.fml.ModContainer;
import mekanism.neoshim.neoforge.client.event.TextureAtlasStitchedEvent;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's TextureAtlasStitchedEvent. */
@Mixin(TextureAtlas.class)
public abstract class TextureAtlasMixin {

    @Inject(method = "upload", at = @At("TAIL"))
    private void mekanism$stitched(SpriteLoader.Preparations preparations, CallbackInfo ci) {
        for (ModContainer mod : ModBusLifecycle.mods()) {
            mod.getEventBus().post(new TextureAtlasStitchedEvent((TextureAtlas) (Object) this));
        }
    }
}
