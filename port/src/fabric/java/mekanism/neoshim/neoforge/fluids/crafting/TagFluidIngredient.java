package mekanism.neoshim.neoforge.fluids.crafting;

import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import mekanism.neoshim.neoforge.fluids.FluidType;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public final class TagFluidIngredient extends FluidIngredient {

    public static final MapCodec<TagFluidIngredient> CODEC = TagKey.codec(Registries.FLUID).xmap(TagFluidIngredient::new, TagFluidIngredient::tag).fieldOf("tag");
    public static final StreamCodec<RegistryFriendlyByteBuf, TagFluidIngredient> STREAM_CODEC = Identifier.STREAM_CODEC
          .map(rl -> new TagFluidIngredient(TagKey.create(Registries.FLUID, rl)), i -> i.tag().location()).cast();
    public static final FluidIngredientTypes.Type<TagFluidIngredient> TYPE = FluidIngredientTypes.register(
          Identifier.fromNamespaceAndPath("neoforge", "tag"), CODEC, STREAM_CODEC);

    private final TagKey<Fluid> tag;

    public TagFluidIngredient(TagKey<Fluid> tag) {
        this.tag = tag;
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return fluidStack.is(tag);
    }

    @Override
    protected Stream<FluidStack> generateStacks() {
        return BuiltInRegistries.FLUID.getTag(tag).stream().flatMap(named -> named.stream()).filter(FluidIngredient::isNotEmptyFluid)
              .map((Holder<Fluid> fluid) -> new FluidStack(fluid, FluidType.BUCKET_VOLUME));
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public FluidIngredientTypes.Type<?> getType() {
        return TYPE;
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof TagFluidIngredient tag && tag.tag.equals(this.tag);
    }

    public TagKey<Fluid> tag() {
        return tag;
    }
}
