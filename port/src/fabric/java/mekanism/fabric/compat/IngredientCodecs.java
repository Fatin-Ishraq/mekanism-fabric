package mekanism.fabric.compat;

import com.mojang.serialization.MapCodec;
import java.util.IdentityHashMap;
import java.util.Map;
import mekanism.neoshim.neoforge.common.crafting.CompoundIngredient;
import mekanism.neoshim.neoforge.common.crafting.DataComponentIngredient;
import mekanism.neoshim.neoforge.common.crafting.DifferenceIngredient;
import mekanism.neoshim.neoforge.common.crafting.ICustomIngredient;
import mekanism.neoshim.neoforge.common.crafting.IngredientType;
import mekanism.neoshim.neoforge.common.crafting.IntersectionIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/**
 * Registers NeoForge-style {@link IngredientType}s as Fabric {@link CustomIngredientSerializer}s under NeoForge's ids. The resource transform in
 * build.gradle renames NeoForge's {@code neoforge:ingredient_type} key to Fabric's {@code fabric:type}, so vanilla's (Fabric-patched) ingredient
 * codec dispatches them.
 */
public final class IngredientCodecs {

    private static final Map<IngredientType<?>, CustomIngredientSerializer<?>> SERIALIZERS = new IdentityHashMap<>();

    static {
        register(Identifier.fromNamespaceAndPath("neoforge", "compound"), CompoundIngredient.TYPE);
        register(Identifier.fromNamespaceAndPath("neoforge", "difference"), DifferenceIngredient.TYPE);
        register(Identifier.fromNamespaceAndPath("neoforge", "intersection"), IntersectionIngredient.TYPE);
        register(Identifier.fromNamespaceAndPath("neoforge", "components"), DataComponentIngredient.TYPE);
    }

    /** Registers a custom ingredient type under the given id (also usable for ingredient types of other mods). */
    public static synchronized <T extends ICustomIngredient> CustomIngredientSerializer<T> register(Identifier id, IngredientType<T> type) {
        CustomIngredientSerializer<T> serializer = new Serializer<>(id, type);
        if (SERIALIZERS.putIfAbsent(type, serializer) != null) {
            throw new IllegalStateException("Ingredient type registered twice: " + id);
        }
        CustomIngredientSerializer.register(serializer);
        return serializer;
    }

    @SuppressWarnings("unchecked")
    public static synchronized <T extends ICustomIngredient> CustomIngredientSerializer<T> serializer(IngredientType<T> type) {
        CustomIngredientSerializer<?> serializer = SERIALIZERS.get(type);
        if (serializer == null) {
            throw new IllegalStateException("Unregistered ingredient type " + type);
        }
        return (CustomIngredientSerializer<T>) serializer;
    }

    /** Registers the NeoForge custom ingredient types with Fabric and the fluid slot display types. Called by the bootstrap. */
    public static void registerNeoForgeIngredientTypes() {
        //Item ingredient types are registered by the static initializer
        mekanism.neoshim.neoforge.fluids.crafting.FluidIngredientCodecs.registerSlotDisplays();
    }

    private record Serializer<T extends ICustomIngredient>(Identifier id, IngredientType<T> type) implements CustomIngredientSerializer<T> {

        @Override
        public Identifier getIdentifier() {
            return id;
        }

        @Override
        public MapCodec<T> getCodec() {
            return type.codec();
        }

        @Override
        @SuppressWarnings("unchecked")
        public StreamCodec<RegistryFriendlyByteBuf, T> getStreamCodec() {
            return (StreamCodec<RegistryFriendlyByteBuf, T>) type.streamCodec();
        }
    }

    private IngredientCodecs() {
    }
}
