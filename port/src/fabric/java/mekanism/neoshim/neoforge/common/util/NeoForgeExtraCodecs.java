package mekanism.neoshim.neoforge.common.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.function.Function;
import java.util.stream.Stream;

public class NeoForgeExtraCodecs {

    /** Field codec that reads {@code name} or, failing that, {@code alias}; always writes {@code name}. */
    public static <T> MapCodec<T> aliasedFieldOf(Codec<T> codec, String name, String alias) {
        return Codec.mapEither(codec.fieldOf(name), codec.fieldOf(alias)).xmap(Either::unwrap, Either::left);
    }

    /** Exactly one of the two map codecs must decode. */
    public static <F, S> MapCodec<Either<F, S>> xor(MapCodec<F> first, MapCodec<S> second) {
        return new MapCodec<>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.concat(first.keys(ops), second.keys(ops)).distinct();
            }

            @Override
            public <T> DataResult<Either<F, S>> decode(DynamicOps<T> ops, MapLike<T> input) {
                DataResult<F> f = first.decode(ops, input);
                DataResult<S> s = second.decode(ops, input);
                if (f.isSuccess() && s.isSuccess()) {
                    return DataResult.error(() -> "Only one of the alternatives may be present");
                } else if (f.isSuccess()) {
                    return f.map(Either::left);
                } else if (s.isSuccess()) {
                    return s.map(Either::right);
                }
                return DataResult.error(() -> "Neither alternative matched");
            }

            @Override
            public <T> RecordBuilder<T> encode(Either<F, S> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                return input.map(f -> first.encode(f, ops, prefix), s -> second.encode(s, ops, prefix));
            }
        };
    }

    /** Dispatches on a "type" key if present (left), otherwise decodes with {@code fallback} (right). */
    public static <K, V, F> MapCodec<Either<V, F>> dispatchMapOrElse(Codec<K> typeCodec, Function<? super V, ? extends K> type,
          Function<? super K, ? extends MapCodec<? extends V>> codec, MapCodec<F> fallback) {
        MapCodec<V> dispatch = typeCodec.dispatchMap(type, codec);
        return new MapCodec<>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.concat(dispatch.keys(ops), fallback.keys(ops)).distinct();
            }

            @Override
            public <T> DataResult<Either<V, F>> decode(DynamicOps<T> ops, MapLike<T> input) {
                if (input.get("type") != null) {
                    return dispatch.decode(ops, input).map(Either::left);
                }
                return fallback.decode(ops, input).map(Either::right);
            }

            @Override
            public <T> RecordBuilder<T> encode(Either<V, F> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                return input.map(v -> dispatch.encode(v, ops, prefix), f -> fallback.encode(f, ops, prefix));
            }
        };
    }


    public static <T> MapCodec<T> withAlternative(MapCodec<T> codec, MapCodec<T> alternative) {
        return Codec.mapEither(codec, alternative).xmap(Either::unwrap, Either::left);
    }

    public static <T> MapCodec<T> mapWithAlternative(MapCodec<T> mapCodec, MapCodec<? extends T> alternative) {
        return Codec.mapEither(mapCodec, alternative).xmap(either -> either.map(v -> v, v -> v), Either::left);
    }
}
