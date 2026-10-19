package mekanism.fabric.compat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Map;
import mekanism.neoshim.fml.ModList;
import mekanism.neoshim.neoforge.common.conditions.ICondition;
import mekanism.neoshim.neoforge.registries.NeoForgeRegistries;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Exposes NeoForge-style {@link ICondition}s (Mekanism's own, registered into the shim's condition registry) as Fabric resource conditions
 * with the same ids, so the converted {@code fabric:load_conditions} entries (see build.gradle D11 transform) resolve. Also registers
 * NeoForge's built-in conditions into the shim registry so conditions nested inside Mekanism conditions keep NeoForge's format.
 */
public final class ConditionBridge {

    private ConditionBridge() {
    }

    /** Called after RegisterEvents so that the shim condition registry is populated. */
    public static void registerAll() {
        for (Map.Entry<ResourceKey<MapCodec<? extends ICondition>>, MapCodec<? extends ICondition>> entry : NeoForgeRegistries.CONDITION_SERIALIZERS.entrySet()) {
            register(entry.getKey().identifier(), entry.getValue());
        }
    }

    /** Registers NeoForge's built-in condition types into the shim registry (namespace "neoforge"). Must run during registration. */
    public static void registerBuiltins() {
        reg("true", MapCodec.unit(new Constant(true)));
        reg("false", MapCodec.unit(new Constant(false)));
        reg("mod_loaded", Codec.STRING.fieldOf("modid").xmap(ModLoaded::new, ModLoaded::modid));
        reg("not", ICondition.CODEC.fieldOf("value").xmap(Not::new, Not::value));
        reg("and", ICondition.CODEC.listOf().fieldOf("values").xmap(And::new, And::values));
        reg("or", ICondition.CODEC.listOf().fieldOf("values").xmap(Or::new, Or::values));
        reg("item_exists", Identifier.CODEC.fieldOf("item").xmap(ItemExists::new, ItemExists::item));
    }

    private static void reg(String path, MapCodec<? extends ICondition> codec) {
        Registry.register(NeoForgeRegistries.CONDITION_SERIALIZERS, Identifier.fromNamespaceAndPath("neoforge", path), codec);
    }

    @SuppressWarnings("unchecked")
    private static <C extends ICondition> void register(Identifier id, MapCodec<C> codec) {
        if (id.getNamespace().equals("neoforge") || ResourceConditions.getConditionType(id) != null) {
            return;
        }
        ResourceConditionType<?>[] holder = new ResourceConditionType<?>[1];
        MapCodec<Wrapped> wrapped = codec.xmap(condition -> new Wrapped(condition, holder[0]), w -> (C) w.condition());
        holder[0] = ResourceConditionType.create(id, wrapped);
        ResourceConditions.register((ResourceConditionType<Wrapped>) holder[0]);
    }

    private record Wrapped(ICondition condition, ResourceConditionType<?> type) implements ResourceCondition {

        @Override
        public ResourceConditionType<?> getType() {
            return type;
        }

        @Override
        public boolean test(@Nullable net.minecraft.core.HolderLookup.Provider registryLookup) {
            return condition.test(ICondition.IContext.EMPTY);
        }
    }

    private record Constant(boolean value) implements ICondition {

        @Override
        public boolean test(IContext context) {
            return value;
        }

        @Override
        public MapCodec<? extends ICondition> codec() {
            return MapCodec.unit(this);
        }
    }

    private record ModLoaded(String modid) implements ICondition {

        @Override
        public boolean test(IContext context) {
            return ModList.get().isLoaded(modid);
        }

        @Override
        public MapCodec<? extends ICondition> codec() {
            return NeoForgeRegistries.CONDITION_SERIALIZERS.get(Identifier.fromNamespaceAndPath("neoforge", "mod_loaded"));
        }
    }

    private record Not(ICondition value) implements ICondition {

        @Override
        public boolean test(IContext context) {
            return !value.test(context);
        }

        @Override
        public MapCodec<? extends ICondition> codec() {
            return NeoForgeRegistries.CONDITION_SERIALIZERS.get(Identifier.fromNamespaceAndPath("neoforge", "not"));
        }
    }

    private record And(List<ICondition> values) implements ICondition {

        @Override
        public boolean test(IContext context) {
            return values.stream().allMatch(c -> c.test(context));
        }

        @Override
        public MapCodec<? extends ICondition> codec() {
            return NeoForgeRegistries.CONDITION_SERIALIZERS.get(Identifier.fromNamespaceAndPath("neoforge", "and"));
        }
    }

    private record Or(List<ICondition> values) implements ICondition {

        @Override
        public boolean test(IContext context) {
            return values.stream().anyMatch(c -> c.test(context));
        }

        @Override
        public MapCodec<? extends ICondition> codec() {
            return NeoForgeRegistries.CONDITION_SERIALIZERS.get(Identifier.fromNamespaceAndPath("neoforge", "or"));
        }
    }

    private record ItemExists(Identifier item) implements ICondition {

        @Override
        public boolean test(IContext context) {
            return BuiltInRegistries.ITEM.containsKey(item);
        }

        @Override
        public MapCodec<? extends ICondition> codec() {
            return NeoForgeRegistries.CONDITION_SERIALIZERS.get(Identifier.fromNamespaceAndPath("neoforge", "item_exists"));
        }
    }
}
