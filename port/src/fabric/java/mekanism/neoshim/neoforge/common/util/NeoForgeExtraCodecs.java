package mekanism.neoshim.neoforge.common.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public class NeoForgeExtraCodecs {

    public static <T> Codec<T> withAlternative(Codec<T> codec, Codec<T> alternative) {
        return Codec.either(codec, alternative).xmap(Either::unwrap, Either::left);
    }

    public static <T> MapCodec<T> withAlternative(MapCodec<T> codec, MapCodec<T> alternative) {
        return Codec.mapEither(codec, alternative).xmap(Either::unwrap, Either::left);
    }

    public static <T> MapCodec<T> mapWithAlternative(MapCodec<T> mapCodec, MapCodec<? extends T> alternative) {
        return Codec.mapEither(mapCodec, alternative).xmap(either -> either.map(v -> v, v -> v), Either::left);
    }
}
