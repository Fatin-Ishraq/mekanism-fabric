package mekanism.neoshim.neoforge.fluids;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

/** An immutable, non-empty fluid stack (the fluid counterpart of vanilla's {@code ItemStackTemplate}). */
public record FluidStackTemplate(Holder<Fluid> fluid, int amount, DataComponentPatch components) implements FluidInstance {

    public static final MapCodec<FluidStackTemplate> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
          BuiltInRegistries.FLUID.holderByNameCodec().fieldOf(FIELD_ID).forGetter(FluidStackTemplate::fluid),
          ExtraCodecs.POSITIVE_INT.fieldOf(FIELD_AMOUNT).forGetter(FluidStackTemplate::amount),
          DataComponentPatch.CODEC.optionalFieldOf(FIELD_COMPONENTS, DataComponentPatch.EMPTY).forGetter(FluidStackTemplate::components)
    ).apply(instance, FluidStackTemplate::new));
    public static final Codec<FluidStackTemplate> CODEC = MAP_CODEC.codec();
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStackTemplate> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.holderRegistry(Registries.FLUID), FluidStackTemplate::fluid,
          ByteBufCodecs.VAR_INT, FluidStackTemplate::amount,
          DataComponentPatch.STREAM_CODEC, FluidStackTemplate::components,
          FluidStackTemplate::new
    );

    public FluidStackTemplate(Holder<Fluid> fluid, int amount) {
        this(fluid, amount, DataComponentPatch.EMPTY);
    }

    public FluidStackTemplate(Fluid fluid, int amount, DataComponentPatch components) {
        this(fluid.builtInRegistryHolder(), amount, components);
    }

    public FluidStackTemplate(Fluid fluid, int amount) {
        this(fluid.builtInRegistryHolder(), amount, DataComponentPatch.EMPTY);
    }

    public static FluidStackTemplate fromNonEmptyStack(FluidStack stack) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Cannot create a template from an empty fluid stack");
        }
        return new FluidStackTemplate(stack.getFluidHolder(), stack.getAmount(), stack.getComponentsPatch());
    }

    public FluidStackTemplate withAmount(int amount) {
        return new FluidStackTemplate(fluid, amount, components);
    }

    public FluidStack create() {
        return new FluidStack(fluid, amount, components);
    }

    public FluidStack apply(DataComponentPatch additionalPatch) {
        return apply(amount, additionalPatch);
    }

    public FluidStack apply(int amount, DataComponentPatch additionalPatch) {
        FluidStack stack = new FluidStack(fluid, amount, components);
        stack.applyComponents(additionalPatch);
        return stack;
    }

    @Override
    public Holder<Fluid> typeHolder() {
        return fluid;
    }

    @Override
    @Nullable
    public <T> T get(DataComponentType<? extends T> type) {
        return components.get(net.minecraft.core.component.DataComponentMap.EMPTY, type);
    }

    public static Codec<FluidStackTemplate> fixedAmountCodec(int amount) {
        return RecordCodecBuilder.create(instance -> instance.group(
              BuiltInRegistries.FLUID.holderByNameCodec().fieldOf(FIELD_ID).forGetter(FluidStackTemplate::fluid),
              DataComponentPatch.CODEC.optionalFieldOf(FIELD_COMPONENTS, DataComponentPatch.EMPTY).forGetter(FluidStackTemplate::components)
        ).apply(instance, (fluid, components) -> new FluidStackTemplate(fluid, amount, components)));
    }

    public static StreamCodec<RegistryFriendlyByteBuf, FluidStackTemplate> fixedAmountStreamCodec(int amount) {
        return StreamCodec.composite(
              ByteBufCodecs.holderRegistry(Registries.FLUID), FluidStackTemplate::fluid,
              DataComponentPatch.STREAM_CODEC, FluidStackTemplate::components,
              (fluid, components) -> new FluidStackTemplate(fluid, amount, components)
        );
    }
}
