package mekanism.neoshim.neoforge.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

/** Convention ("c") tags with the same ids as NeoForge's; Fabric uses the same convention namespace. */
public class Tags {

    public static class Blocks {

        public static final TagKey<Block> ORES = TagKey.create(Registries.BLOCK, c("ores"));
    }

    public static class Items {

        public static final TagKey<Item> ORES = TagKey.create(Registries.ITEM, c("ores"));
        public static final TagKey<Item> ORES_COPPER = TagKey.create(Registries.ITEM, c("ores/copper"));
        public static final TagKey<Item> ORES_GOLD = TagKey.create(Registries.ITEM, c("ores/gold"));
        public static final TagKey<Item> ORES_IRON = TagKey.create(Registries.ITEM, c("ores/iron"));
    }

    public static class Fluids {

        public static final TagKey<Fluid> GASEOUS = TagKey.create(Registries.FLUID, c("gaseous"));
        public static final TagKey<Fluid> HIDDEN_FROM_RECIPE_VIEWERS = TagKey.create(Registries.FLUID, c("hidden_from_recipe_viewers"));
    }

    public static class EntityTypes {

        public static final TagKey<EntityType<?>> TELEPORTING_NOT_SUPPORTED = TagKey.create(Registries.ENTITY_TYPE, c("teleporting_not_supported"));
    }

    public static class DamageTypes {

        public static final TagKey<DamageType> IS_TECHNICAL = TagKey.create(Registries.DAMAGE_TYPE, c("is_technical"));
    }

    private static ResourceLocation c(String path) {
        return ResourceLocation.fromNamespaceAndPath("c", path);
    }
}
