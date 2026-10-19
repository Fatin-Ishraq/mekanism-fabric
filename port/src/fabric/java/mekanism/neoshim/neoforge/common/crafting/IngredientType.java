package mekanism.neoshim.neoforge.common.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/** Codecs of a custom ingredient type. Registered with Fabric's custom ingredient serializers by {@link mekanism.fabric.compat.IngredientCodecs}. */
public record IngredientType<T extends ICustomIngredient>(MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {

    public IngredientType(MapCodec<T> codec) {
        this(codec, net.minecraft.network.codec.ByteBufCodecs.fromCodecWithRegistries(codec.codec()));
    }
}
