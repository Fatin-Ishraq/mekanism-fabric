package mekanism.neoshim.neoforge.transfer.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import mekanism.neoshim.neoforge.fluids.FluidStackTemplate;
import mekanism.neoshim.neoforge.fluids.FluidType;
import mekanism.neoshim.neoforge.transfer.resource.DataComponentHolderResource;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/** A fluid type plus its component patch, without an amount (Fabric: {@link FluidVariant}). Amounts that go with it are in millibuckets. */
public final class FluidResource implements DataComponentHolderResource<Fluid> {

    public static final FluidResource EMPTY = new FluidResource(Fluids.EMPTY.builtInRegistryHolder(), DataComponentPatch.EMPTY);

    private static final Codec<FluidResource> NON_EMPTY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
          BuiltInRegistries.FLUID.holderByNameCodec().fieldOf("id").forGetter(FluidResource::typeHolder),
          DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(FluidResource::getComponentsPatch)
    ).apply(instance, FluidResource::of));
    public static final Codec<FluidResource> CODEC = NON_EMPTY_CODEC.validate(resource -> resource.isEmpty()
          ? com.mojang.serialization.DataResult.error(() -> "Fluid resource must not be empty") : com.mojang.serialization.DataResult.success(resource));
    public static final Codec<FluidResource> OPTIONAL_CODEC = NON_EMPTY_CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidResource> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.holderRegistry(Registries.FLUID), FluidResource::typeHolder,
          DataComponentPatch.STREAM_CODEC, FluidResource::getComponentsPatch,
          FluidResource::of
    );

    private final Holder<Fluid> fluid;
    private final DataComponentPatch patch;
    private DataComponentMap components;

    private FluidResource(Holder<Fluid> fluid, DataComponentPatch patch) {
        this.fluid = fluid;
        this.patch = patch;
    }

    public static FluidResource of(FluidStack stack) {
        return stack.isEmpty() ? EMPTY : of(stack.getFluidHolder(), stack.getComponentsPatch());
    }

    public static FluidResource of(FluidStackTemplate template) {
        return of(template.fluid(), template.components());
    }

    public static FluidResource of(Fluid fluid) {
        return of(fluid.builtInRegistryHolder(), DataComponentPatch.EMPTY);
    }

    public static FluidResource of(Fluid fluid, DataComponentPatch patch) {
        return of(fluid.builtInRegistryHolder(), patch);
    }

    public static FluidResource of(Holder<Fluid> fluid) {
        return of(fluid, DataComponentPatch.EMPTY);
    }

    public static FluidResource of(Holder<Fluid> fluid, DataComponentPatch patch) {
        if (fluid.value() == Fluids.EMPTY) {
            return EMPTY;
        }
        return new FluidResource(fluid, patch);
    }

    public static FluidResource of(FluidVariant variant) {
        return variant.isBlank() ? EMPTY : of(variant.typeHolder(), variant.getComponentsPatch());
    }

    public FluidVariant toVariant() {
        return isEmpty() ? FluidVariant.blank() : FluidVariant.of(getFluid(), patch);
    }

    @Override
    public Fluid value() {
        return fluid.value();
    }

    public Fluid getFluid() {
        return fluid.value();
    }

    @Override
    public Holder<Fluid> typeHolder() {
        return fluid;
    }

    public FluidType getFluidType() {
        return getFluid().getFluidType();
    }

    @Override
    public boolean isEmpty() {
        return fluid.value() == Fluids.EMPTY;
    }

    @Override
    public FluidResource withMergedPatch(DataComponentPatch patch) {
        PatchedDataComponentMap map = PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, this.patch);
        map.applyPatch(patch);
        return of(fluid, map.asPatch());
    }

    @Override
    public <D> FluidResource with(DataComponentType<D> type, D value) {
        return withMergedPatch(DataComponentPatch.builder().set(type, value).build());
    }

    @Override
    public <D> FluidResource with(Supplier<? extends DataComponentType<D>> type, D value) {
        return with(type.get(), value);
    }

    @Override
    public FluidResource without(DataComponentType<?> type) {
        return withMergedPatch(DataComponentPatch.builder().remove(type).build());
    }

    @Override
    public FluidResource without(Supplier<? extends DataComponentType<?>> type) {
        return without(type.get());
    }

    @Override
    public DataComponentMap getComponents() {
        if (components == null) {
            components = PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, patch);
        }
        return components;
    }

    @Override
    public DataComponentPatch getComponentsPatch() {
        return patch;
    }

    public FluidStack toStack(int amount) {
        return isEmpty() || amount <= 0 ? FluidStack.EMPTY : new FluidStack(fluid, amount, patch);
    }

    @Override
    public boolean isComponentsPatchEmpty() {
        return patch.isEmpty();
    }

    public boolean is(FluidType type) {
        return getFluidType() == type;
    }

    public boolean matches(FluidStack stack) {
        return stack.getFluid() == getFluid() && stack.getComponentsPatch().equals(patch);
    }

    public boolean matches(FluidStackTemplate template) {
        return template.fluid().value() == getFluid() && template.components().equals(patch);
    }

    public boolean test(Predicate<FluidStack> predicate) {
        return predicate.test(toStack(1));
    }

    public Component getHoverName() {
        return toStack(1).getHoverName();
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof FluidResource other && fluid.value() == other.fluid.value() && patch.equals(other.patch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fluid.value(), patch);
    }

    @Override
    public String toString() {
        return "FluidResource[" + fluid.getRegisteredName() + (patch.isEmpty() ? "" : " " + patch) + "]";
    }
}
