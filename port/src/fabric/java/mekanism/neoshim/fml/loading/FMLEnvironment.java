package mekanism.neoshim.fml.loading;

import mekanism.neoshim.api.distmarker.Dist;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public class FMLEnvironment {

    public static final Dist dist = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? Dist.CLIENT : Dist.DEDICATED_SERVER;
    public static final boolean production = !FabricLoader.getInstance().isDevelopmentEnvironment();
}
