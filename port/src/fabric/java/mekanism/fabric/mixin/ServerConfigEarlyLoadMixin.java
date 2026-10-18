package mekanism.fabric.mixin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NeoForge loads server configs before it runs biome modifiers; Fabric applies biome modifications at the end of the MinecraftServer constructor, but Forge Config
 * API Port only loads server configs at SERVER_STARTING. Load them (from the same locations FCAP uses) just before Fabric's biome modification pass (priority 900
 * &lt; Fabric's default 1000), and unload them right after it in ServerConfigEarlyUnloadMixin so FCAP's regular load still happens as usual.
 */
@Mixin(value = MinecraftServer.class, priority = 900)
public abstract class ServerConfigEarlyLoadMixin {

    @Unique
    private static final LevelResource SERVER_CONFIG = new LevelResource("serverconfig");

    @Inject(method = "<init>", at = @At("RETURN"))
    private void mekanism$loadServerConfigsForBiomeModifiers(CallbackInfo ci) {
        Path serverConfig = ((MinecraftServer) (Object) this).getWorldPath(SERVER_CONFIG);
        try {
            Files.createDirectories(serverConfig);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.SERVER, FabricLoader.getInstance().getConfigDir(), serverConfig);
    }
}
