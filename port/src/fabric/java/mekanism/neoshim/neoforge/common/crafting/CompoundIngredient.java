package mekanism.neoshim.neoforge.common.crafting;

import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import mekanism.fabric.compat.IngredientCodecs;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** Union of ingredients. Serialized like NeoForge: {@code {"type": "neoforge:compound", "children": [...]}}. */
public record CompoundIngredient(List<Ingredient> children) implements ICustomIngredient {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("neoforge", "compound");
    public static final MapCodec<CompoundIngredient> CODEC = IngredientCodecs.LIST_CODEC_NONEMPTY.fieldOf("children").xmap(CompoundIngredient::new, CompoundIngredient::children);
    public static final StreamCodec<RegistryFriendlyByteBuf, CompoundIngredient> STREAM_CODEC = Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list())
          .map(CompoundIngredient::new, CompoundIngredient::children);
    public static final CustomIngredientSerializer<CompoundIngredient> SERIALIZER = IngredientCodecs.serializer(ID, CODEC, STREAM_CODEC);

    public CompoundIngredient {
        if (children.isEmpty()) {
            throw new IllegalArgumentException("Compound ingredient must have at least one child");
        }
    }

    public static Ingredient of(Ingredient... children) {
        if (children.length == 1) {
            return children[0];
        }
        return of(Arrays.asList(children));
    }

    public static Ingredient of(List<Ingredient> children) {
        if (children.size() == 1) {
            return children.getFirst();
        }
        return new CompoundIngredient(List.copyOf(children)).toVanilla();
    }

    @Override
    public Stream<ItemStack> getItems() {
        return children.stream().flatMap(child -> Arrays.stream(child.getItems()));
    }

    @Override
    public boolean test(ItemStack stack) {
        for (Ingredient child : children) {
            if (child.test(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSimple() {
        for (Ingredient child : children) {
            if (child.requiresTesting()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
