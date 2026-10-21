package mekanism.neoshim.neoforge.registries;

import com.mojang.serialization.MapCodec;
import mekanism.neoshim.neoforge.attachment.AttachmentType;
import mekanism.neoshim.neoforge.common.conditions.ICondition;
import mekanism.neoshim.neoforge.common.world.BiomeModifier;
import mekanism.neoshim.neoforge.common.world.StructureModifier;
import mekanism.neoshim.neoforge.fluids.FluidType;
import net.minecraft.core.Registry;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

/**
 * Registry keys for the NeoForge-only registries Mekanism references. They live in the {@code mekanism_fabric} namespace so they cannot clash
 * with anything else on Fabric. Registries that Fabric has no equivalent of are created lazily by the shim (see {@link #FLUID_TYPES}).
 */
public class NeoForgeRegistries {

    public static final Registry<FluidType> FLUID_TYPES = new RegistryBuilder<>(Keys.FLUID_TYPES).create();
    public static final Registry<AttachmentType<?>> ATTACHMENT_TYPES = new RegistryBuilder<>(Keys.ATTACHMENT_TYPES).create();
    public static final Registry<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = new RegistryBuilder<>(Keys.ENTITY_DATA_SERIALIZERS).create();
    public static final Registry<MapCodec<? extends ICondition>> CONDITION_SERIALIZERS = new RegistryBuilder<>(Keys.CONDITION_CODECS).create();
    public static final Registry<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = new RegistryBuilder<>(Keys.BIOME_MODIFIER_SERIALIZERS).create();
    public static final Registry<MapCodec<? extends StructureModifier>> STRUCTURE_MODIFIER_SERIALIZERS = new RegistryBuilder<>(Keys.STRUCTURE_MODIFIER_SERIALIZERS).create();

    public static final class Keys {

        public static final ResourceKey<Registry<FluidType>> FLUID_TYPES = key("fluid_type");
        public static final ResourceKey<Registry<AttachmentType<?>>> ATTACHMENT_TYPES = key("attachment_types");
        public static final ResourceKey<Registry<EntityDataSerializer<?>>> ENTITY_DATA_SERIALIZERS = key("entity_data_serializers");
        public static final ResourceKey<Registry<MapCodec<? extends ICondition>>> CONDITION_CODECS = key("condition_codecs");
        public static final ResourceKey<Registry<MapCodec<? extends BiomeModifier>>> BIOME_MODIFIER_SERIALIZERS = key("biome_modifier_serializers");
        public static final ResourceKey<Registry<MapCodec<? extends StructureModifier>>> STRUCTURE_MODIFIER_SERIALIZERS = key("structure_modifier_serializers");
        public static final ResourceKey<Registry<BiomeModifier>> BIOME_MODIFIERS = key("biome_modifier");
        public static final ResourceKey<Registry<StructureModifier>> STRUCTURE_MODIFIERS = key("structure_modifier");

        private static <T> ResourceKey<Registry<T>> key(String name) {
            return ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("mekanism_fabric", "neoforge/" + name));
        }
    }
}
