package mekanism.neoshim.neoforge.fluids;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

/**
 * Fluid + amount (in mB) + data components. Serialized format ({@code id}, {@code amount}, {@code components}) matches NeoForge so that
 * saved data is interchangeable.
 */
public final class FluidStack implements DataComponentHolder {

    public static final Codec<Holder<Fluid>> FLUID_NON_EMPTY_CODEC = BuiltInRegistries.FLUID.holderByNameCodec().validate(
          holder -> holder.is(Fluids.EMPTY.builtInRegistryHolder()) ? DataResult.error(() -> "Fluid must not be minecraft:empty") : DataResult.success(holder));

    public static final Codec<FluidStack> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
          FLUID_NON_EMPTY_CODEC.fieldOf("id").forGetter(FluidStack::getFluidHolder),
          ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(FluidStack::getAmount),
          DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(stack -> stack.components.asPatch())
    ).apply(instance, FluidStack::new)));

    public static final Codec<FluidStack> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC)
          .xmap(optional -> optional.orElse(FluidStack.EMPTY), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> OPTIONAL_STREAM_CODEC = new StreamCodec<>() {
        private static final StreamCodec<RegistryFriendlyByteBuf, Holder<Fluid>> FLUID_STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.FLUID);

        @Override
        public FluidStack decode(RegistryFriendlyByteBuf buf) {
            int amount = buf.readVarInt();
            if (amount <= 0) {
                return FluidStack.EMPTY;
            }
            Holder<Fluid> holder = FLUID_STREAM_CODEC.decode(buf);
            DataComponentPatch patch = DataComponentPatch.STREAM_CODEC.decode(buf);
            return new FluidStack(holder, amount, patch);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, FluidStack stack) {
            if (stack.isEmpty()) {
                buf.writeVarInt(0);
            } else {
                buf.writeVarInt(stack.getAmount());
                FLUID_STREAM_CODEC.encode(buf, stack.getFluidHolder());
                DataComponentPatch.STREAM_CODEC.encode(buf, stack.components.asPatch());
            }
        }
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public FluidStack decode(RegistryFriendlyByteBuf buf) {
            FluidStack stack = OPTIONAL_STREAM_CODEC.decode(buf);
            if (stack.isEmpty()) {
                throw new DecoderException("Empty FluidStack not allowed");
            }
            return stack;
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, FluidStack stack) {
            if (stack.isEmpty()) {
                throw new EncoderException("Empty FluidStack not allowed");
            }
            OPTIONAL_STREAM_CODEC.encode(buf, stack);
        }
    };

    public static final FluidStack EMPTY = new FluidStack(null);

    private int amount;
    private final Fluid fluid;
    private final PatchedDataComponentMap components;

    public static MapCodec<FluidStack> fixedAmountCodec(int amount) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
              FLUID_NON_EMPTY_CODEC.fieldOf("id").forGetter(FluidStack::getFluidHolder),
              DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(stack -> stack.components.asPatch())
        ).apply(instance, (holder, patch) -> new FluidStack(holder, amount, patch)));
    }

    public FluidStack(Holder<Fluid> fluid, int amount) {
        this(fluid.value(), amount, new PatchedDataComponentMap(DataComponentMap.EMPTY));
    }

    public FluidStack(Holder<Fluid> fluid, int amount, DataComponentPatch patch) {
        this(fluid.value(), amount, PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, patch));
    }

    public FluidStack(Fluid fluid, int amount) {
        this(fluid, amount, new PatchedDataComponentMap(DataComponentMap.EMPTY));
    }

    private FluidStack(Fluid fluid, int amount, PatchedDataComponentMap components) {
        this.fluid = fluid;
        this.amount = amount;
        this.components = components;
    }

    private FluidStack(@Nullable Void unused) {
        this.fluid = null;
        this.components = new PatchedDataComponentMap(DataComponentMap.EMPTY);
    }

    public static Optional<FluidStack> parse(HolderLookup.Provider lookupProvider, Tag tag) {
        return CODEC.parse(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag).resultOrPartial(error -> {
        });
    }

    public static FluidStack parseOptional(HolderLookup.Provider lookupProvider, CompoundTag tag) {
        return tag.isEmpty() ? EMPTY : parse(lookupProvider, tag).orElse(EMPTY);
    }

    public Tag save(HolderLookup.Provider lookupProvider, Tag prefix) {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot encode empty FluidStack");
        }
        return CODEC.encode(this, lookupProvider.createSerializationContext(NbtOps.INSTANCE), prefix).getOrThrow();
    }

    public Tag save(HolderLookup.Provider lookupProvider) {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot encode empty FluidStack");
        }
        return CODEC.encodeStart(lookupProvider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
    }

    public Tag saveOptional(HolderLookup.Provider lookupProvider) {
        return isEmpty() ? new CompoundTag() : save(lookupProvider, new CompoundTag());
    }

    public boolean isEmpty() {
        return this == EMPTY || this.fluid == Fluids.EMPTY || this.amount <= 0;
    }

    public FluidStack split(int amount) {
        int i = Math.min(amount, this.amount);
        FluidStack fluidStack = this.copyWithAmount(i);
        this.shrink(i);
        return fluidStack;
    }

    public FluidStack copyAndClear() {
        if (this.isEmpty()) {
            return EMPTY;
        }
        FluidStack fluidStack = this.copy();
        this.setAmount(0);
        return fluidStack;
    }

    public Fluid getFluid() {
        return this.isEmpty() ? Fluids.EMPTY : this.fluid;
    }

    @SuppressWarnings("deprecation")
    public Holder<Fluid> getFluidHolder() {
        return this.getFluid().builtInRegistryHolder();
    }

    public boolean is(TagKey<Fluid> tag) {
        return this.getFluidHolder().is(tag);
    }

    public boolean is(Fluid fluid) {
        return this.getFluid() == fluid;
    }

    public boolean is(Predicate<Holder<Fluid>> holderPredicate) {
        return holderPredicate.test(this.getFluidHolder());
    }

    public boolean is(Holder<Fluid> holder) {
        return is(holder.value());
    }

    public boolean is(HolderSet<Fluid> holders) {
        return holders.contains(this.getFluidHolder());
    }

    public boolean is(FluidType fluidType) {
        return getFluidType() == fluidType;
    }

    public Stream<TagKey<Fluid>> getTags() {
        return this.getFluidHolder().tags();
    }

    public FluidStack copy() {
        if (this.isEmpty()) {
            return EMPTY;
        }
        return new FluidStack(this.fluid, this.amount, this.components.copy());
    }

    public FluidStack copyWithAmount(int amount) {
        if (this.isEmpty()) {
            return EMPTY;
        }
        FluidStack fluidStack = this.copy();
        fluidStack.setAmount(amount);
        return fluidStack;
    }

    public int getAmount() {
        return this.isEmpty() ? 0 : this.amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void limitSize(int amount) {
        if (!this.isEmpty() && this.getAmount() > amount) {
            this.setAmount(amount);
        }
    }

    public void grow(int addedAmount) {
        this.setAmount(this.getAmount() + addedAmount);
    }

    public void shrink(int removedAmount) {
        this.grow(-removedAmount);
    }

    @Override
    public DataComponentMap getComponents() {
        return this.components;
    }

    public DataComponentPatch getComponentsPatch() {
        return !this.isEmpty() ? this.components.asPatch() : DataComponentPatch.EMPTY;
    }

    public boolean isComponentsPatchEmpty() {
        return this.isEmpty() || this.components.asPatch().isEmpty();
    }

    @Nullable
    public <T> T set(DataComponentType<? super T> type, @Nullable T component) {
        return this.components.set(type, component);
    }

    @Nullable
    public <T, U> T update(DataComponentType<T> type, T defaultValue, U updateContext, java.util.function.BiFunction<T, U, T> updater) {
        return this.set(type, updater.apply(this.getOrDefault(type, defaultValue), updateContext));
    }

    @Nullable
    public <T> T update(DataComponentType<T> type, T defaultValue, UnaryOperator<T> updater) {
        T t = this.getOrDefault(type, defaultValue);
        return this.set(type, updater.apply(t));
    }

    @Nullable
    public <T> T remove(DataComponentType<? extends T> type) {
        return this.components.remove(type);
    }

    public void applyComponents(DataComponentPatch patch) {
        this.components.applyPatch(patch);
    }

    public void applyComponents(DataComponentMap components) {
        this.components.setAll(components);
    }

    public Component getHoverName() {
        return getFluidType().getDescription(this);
    }

    public String getDescriptionId() {
        return getFluidType().getDescriptionId(this);
    }

    public FluidType getFluidType() {
        return FluidType.of(getFluid());
    }

    @Override
    public String toString() {
        return this.getAmount() + " " + BuiltInRegistries.FLUID.getKey(this.getFluid());
    }

    public static FluidStack copyOf(FluidStack stack) {
        return stack.copy();
    }

    public static boolean matches(FluidStack first, FluidStack second) {
        if (first == second) {
            return true;
        }
        return first.getAmount() == second.getAmount() && isSameFluidSameComponents(first, second);
    }

    public static boolean isSameFluid(FluidStack first, FluidStack second) {
        return first.is(second.getFluid());
    }

    public static boolean isSameFluidSameComponents(FluidStack first, FluidStack second) {
        if (!first.is(second.getFluid())) {
            return false;
        }
        return first.isEmpty() && second.isEmpty() || Objects.equals(first.components, second.components);
    }

    public static MapCodec<FluidStack> lenientOptionalFieldOf(String fieldName) {
        return CODEC.lenientOptionalFieldOf(fieldName).xmap(optional -> optional.orElse(EMPTY), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack));
    }

    public static int hashFluidAndComponents(@Nullable FluidStack stack) {
        if (stack != null) {
            int i = 31 + stack.getFluid().hashCode();
            return 31 * i + stack.getComponents().hashCode();
        }
        return 0;
    }
}
