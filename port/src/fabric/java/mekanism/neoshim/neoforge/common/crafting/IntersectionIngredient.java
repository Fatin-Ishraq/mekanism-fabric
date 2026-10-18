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

/** Intersection of ingredients. Serialized like NeoForge: {@code {"type": "neoforge:intersection", "children": [...]}}. */
public record IntersectionIngredient(List<Ingredient> children) implements ICustomIngredient {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("neoforge", "intersection");
    public static final MapCodec<IntersectionIngredient> CODEC = IngredientCodecs.LIST_CODEC_NONEMPTY.fieldOf("children").xmap(IntersectionIngredient::new, IntersectionIngredient::children);
    public static final StreamCodec<RegistryFriendlyByteBuf, IntersectionIngredient> STREAM_CODEC = Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list())
          .map(IntersectionIngredient::new, IntersectionIngredient::children);
    public static final CustomIngredientSerializer<IntersectionIngredient> SERIALIZER = IngredientCodecs.serializer(ID, CODEC, STREAM_CODEC);

    public static Ingredient of(Ingredient... children) {
        if (children.length == 1) {
            return children[0];
        }
        return new IntersectionIngredient(List.of(children)).toVanilla();
    }

    @Override
    public boolean test(ItemStack stack) {
        for (Ingredient child : children) {
            if (!child.test(stack)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Stream<ItemStack> getItems() {
        return Arrays.stream(children.getFirst().getItems()).filter(this::test);
    }

    @Override
    public boolean isSimple() {
        return children.stream().noneMatch(Ingredient::requiresTesting);
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
