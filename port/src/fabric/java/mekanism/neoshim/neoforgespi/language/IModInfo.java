package mekanism.neoshim.neoforgespi.language;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

/** Subset of NeoForge's IModInfo, implemented as a class. */
public class IModInfo {

    private final String modId;
    private final String fabricModId;

    public IModInfo(String modId, String fabricModId) {
        this.modId = modId;
        this.fabricModId = fabricModId;
    }

    public String getModId() {
        return modId;
    }

    public String getDisplayName() {
        return FabricLoader.getInstance().getModContainer(fabricModId).map(c -> c.getMetadata().getName()).orElse(modId);
    }

    public ArtifactVersion getVersion() {
        String version = FabricLoader.getInstance().getModContainer(fabricModId).map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("0.0.0");
        //Strip Fabric build metadata ("10.7.19+fabric.0" -> "10.7.19") so comparisons match upstream version semantics
        int plus = version.indexOf('+');
        return new DefaultArtifactVersion(plus == -1 ? version : version.substring(0, plus));
    }
}
