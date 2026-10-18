package mekanism.fabric.mixin;

import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** See ServerConfigEarlyLoadMixin: runs after Fabric's biome modification pass (priority 1100). */
@Mixin(value = MinecraftServer.class, priority = 1100)
public abstract class ServerConfigEarlyUnloadMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void mekanism$unloadServerConfigsAfterBiomeModifiers(CallbackInfo ci) {
        ConfigTracker.INSTANCE.unloadConfigs(ModConfig.Type.SERVER);
    }
}
