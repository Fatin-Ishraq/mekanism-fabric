package mekanism.fabric.mixin;

import com.google.common.collect.ImmutableList;
import java.util.List;
import mekanism.neoshim.neoforge.common.extensions.IWeightedListBuilderExtension;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** NeoForge exposes the entries of a {@link WeightedList.Builder}. */
@Mixin(WeightedList.Builder.class)
public abstract class WeightedListBuilderMixin<E> implements IWeightedListBuilderExtension<E> {

    @Shadow
    @Final
    private ImmutableList.Builder<Weighted<E>> result;

    @Override
    public List<Weighted<E>> getList() {
        return result.build();
    }
}
