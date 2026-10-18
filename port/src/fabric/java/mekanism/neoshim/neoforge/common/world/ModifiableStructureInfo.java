package mekanism.neoshim.neoforge.common.world;

public class ModifiableStructureInfo {

    public record StructureInfo(net.minecraft.world.level.levelgen.structure.Structure.StructureSettings structureSettings) {

        public static class Builder {

            private final StructureSettingsBuilder structureSettings;

            public Builder(StructureSettingsBuilder structureSettings) {
                this.structureSettings = structureSettings;
            }

            public StructureSettingsBuilder getStructureSettings() {
                return structureSettings;
            }
        }
    }
}
