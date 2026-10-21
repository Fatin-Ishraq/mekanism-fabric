package mekanism.neoshim.neoforge.client.event;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.minecraft.client.resources.model.sprite.AtlasManager.AtlasConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;

/** Fired on the mod bus before the texture atlases are built; collected by the client bridge and registered with Fabric's {@code AtlasRegistry}. */
public class RegisterTextureAtlasesEvent extends Event implements IModBusEvent {

    private final List<AtlasConfig> atlases = new ArrayList<>();
    private final Map<Identifier, Set<MetadataSectionType<?>>> additionalMetadata = new LinkedHashMap<>();

    public void register(AtlasConfig config) {
        atlases.add(config);
    }

    public void addAdditionalMetadata(Identifier atlas, MetadataSectionType<?> type) {
        additionalMetadata.computeIfAbsent(atlas, id -> new LinkedHashSet<>()).add(type);
    }

    public List<AtlasConfig> getAtlases() {
        List<AtlasConfig> configs = new ArrayList<>(atlases.size());
        for (AtlasConfig config : atlases) {
            Set<MetadataSectionType<?>> extra = additionalMetadata.get(config.definitionLocation());
            if (extra == null || extra.isEmpty()) {
                configs.add(config);
            } else {
                Set<MetadataSectionType<?>> all = new LinkedHashSet<>(config.additionalMetadata());
                all.addAll(extra);
                configs.add(new AtlasConfig(config.textureId(), config.definitionLocation(), config.createMipmaps(), all));
            }
        }
        return configs;
    }
}
