package mekanism.neoshim.neoforge.common.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import mekanism.neoshim.neoforge.registries.NeoForgeRegistries;

/** Load condition. Mekanism's custom conditions are registered as Fabric resource conditions by the bootstrap. */
public interface ICondition {

    Codec<ICondition> CODEC = Codec.lazyInitialized(() -> NeoForgeRegistries.CONDITION_SERIALIZERS.byNameCodec().dispatch("type", ICondition::codec, Function.identity()));

    boolean test(IContext context);

    MapCodec<? extends ICondition> codec();

    interface IContext {

        IContext EMPTY = new IContext() {
        };
    }

}
