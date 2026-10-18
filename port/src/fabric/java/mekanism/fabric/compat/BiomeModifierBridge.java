package mekanism.fabric.compat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Stream;
import mekanism.common.Mekanism;
import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Applies the NeoForge biome modifiers shipped in our jar ({@code data/<ns>/neoforge/biome_modifier/*.json}, type
 * {@code neoforge:add_features}) through Fabric's BiomeModifications API. Features and biome tags themselves stay data-driven. Limitation:
 * datapacks cannot override these modifier files (they are read from the mod jar at startup).
 */
public final class BiomeModifierBridge {

    private BiomeModifierBridge() {
    }

    public static void apply(String fabricModId) {
        ModContainer container = FabricLoader.getInstance().getModContainer(fabricModId).orElseThrow();
        for (Path root : container.getRootPaths()) {
            Path data = root.resolve("data");
            if (!Files.isDirectory(data)) {
                continue;
            }
            try (Stream<Path> namespaces = Files.list(data)) {
                for (Path namespace : namespaces.toList()) {
                    Path modifiers = namespace.resolve("neoforge").resolve("biome_modifier");
                    if (!Files.isDirectory(modifiers)) {
                        continue;
                    }
                    try (Stream<Path> files = Files.list(modifiers)) {
                        for (Path file : files.filter(p -> p.toString().endsWith(".json")).toList()) {
                            applyFile(namespace.getFileName().toString().replace("/", ""), file);
                        }
                    }
                }
            } catch (IOException e) {
                Mekanism.logger.error("[Fabric port] Failed to read biome modifiers from {}", root, e);
            }
        }
    }

    private static void applyFile(String namespace, Path file) throws IOException {
        JsonObject json;
        try (Reader reader = Files.newBufferedReader(file)) {
            json = JsonParser.parseReader(reader).getAsJsonObject();
        }
        String type = json.get("type").getAsString();
        String name = file.getFileName().toString().replace(".json", "");
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, name);
        if (!type.equals("neoforge:add_features")) {
            Mekanism.logger.warn("[Fabric port] Unsupported biome modifier type {} in {}", type, id);
            return;
        }
        Predicate<BiomeSelectionContext> biomes = biomeSelector(json.get("biomes"));
        GenerationStep.Decoration step = GenerationStep.Decoration.valueOf(json.get("step").getAsString().toUpperCase(Locale.ROOT));
        List<ResourceKey<PlacedFeature>> features = new ArrayList<>();
        JsonElement featureElement = json.get("features");
        if (featureElement.isJsonArray()) {
            for (JsonElement element : featureElement.getAsJsonArray()) {
                features.add(ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.parse(element.getAsString())));
            }
        } else {
            features.add(ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.parse(featureElement.getAsString())));
        }
        BiomeModifications.create(id).add(ModificationPhase.ADDITIONS, biomes, (BiomeModificationContext context) -> {
            for (ResourceKey<PlacedFeature> feature : features) {
                context.getGenerationSettings().addFeature(step, feature);
            }
        });
        Mekanism.logger.debug("[Fabric port] Applied biome modifier {} ({} features)", id, features.size());
    }

    private static Predicate<BiomeSelectionContext> biomeSelector(JsonElement biomes) {
        if (biomes.isJsonPrimitive()) {
            String value = biomes.getAsString();
            if (value.startsWith("#")) {
                return BiomeSelectors.tag(TagKey.create(Registries.BIOME, ResourceLocation.parse(value.substring(1))));
            }
            return BiomeSelectors.includeByKey(ResourceKey.create(Registries.BIOME, ResourceLocation.parse(value)));
        }
        JsonArray array = biomes.getAsJsonArray();
        List<ResourceKey<net.minecraft.world.level.biome.Biome>> keys = new ArrayList<>();
        for (JsonElement element : array) {
            keys.add(ResourceKey.create(Registries.BIOME, ResourceLocation.parse(element.getAsString())));
        }
        return BiomeSelectors.includeByKey(keys);
    }
}
