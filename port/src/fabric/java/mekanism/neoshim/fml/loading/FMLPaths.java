package mekanism.neoshim.fml.loading;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public enum FMLPaths {
    GAMEDIR,
    MODSDIR,
    CONFIGDIR;

    public Path get() {
        FabricLoader loader = FabricLoader.getInstance();
        return switch (this) {
            case GAMEDIR -> loader.getGameDir();
            case MODSDIR -> loader.getGameDir().resolve("mods");
            case CONFIGDIR -> loader.getConfigDir();
        };
    }

    public static Path getOrCreateGameRelativePath(Path path) {
        Path gameFolderPath = GAMEDIR.get().resolve(path);
        if (!Files.isDirectory(gameFolderPath)) {
            try {
                Files.createDirectories(gameFolderPath);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return gameFolderPath;
    }
}
