package mekanism.neoshim.neoforge.attachment;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.neoshim.neoforge.common.util.INBTSerializable;
import mekanism.neoshim.neoforge.server.ServerLifecycleHooks;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge data attachment type, backed by a Fabric attachment (NOTES.md D5). The Fabric attachment is created when the type is registered
 * into {@code NeoForgeRegistries.ATTACHMENT_TYPES} (see {@link #bind}).
 */
public final class AttachmentType<T> {

    private final Function<Object, T> defaultValueSupplier;
    @Nullable
    private final Codec<T> codec;
    private final boolean copyOnDeath;
    @Nullable
    private net.fabricmc.fabric.api.attachment.v1.AttachmentType<T> fabricType;

    private AttachmentType(Builder<T> builder) {
        this.defaultValueSupplier = builder.defaultValueSupplier;
        this.codec = builder.codec;
        this.copyOnDeath = builder.copyOnDeath;
    }

    public static <T> Builder<T> builder(Supplier<T> defaultValueSupplier) {
        return new Builder<>(holder -> defaultValueSupplier.get());
    }

    public static <T> Builder<T> builder(Function<IAttachmentHolder, T> defaultValueConstructor) {
        return new Builder<>(holder -> defaultValueConstructor.apply((IAttachmentHolder) holder));
    }

    public static <S extends Tag, T extends INBTSerializable<S>> Builder<T> serializable(Supplier<T> defaultValueSupplier) {
        return builder(defaultValueSupplier).serialize(nbtCodec(defaultValueSupplier));
    }

    @SuppressWarnings("unchecked")
    private static <S extends Tag, T extends INBTSerializable<S>> Codec<T> nbtCodec(Supplier<T> factory) {
        return Codec.PASSTHROUGH.xmap(dynamic -> {
            T value = factory.get();
            value.deserializeNBT(provider(), (S) dynamic.convert(NbtOps.INSTANCE).getValue());
            return value;
        }, value -> new com.mojang.serialization.Dynamic<>(NbtOps.INSTANCE, value.serializeNBT(provider())));
    }

    private static HolderLookup.Provider provider() {
        net.minecraft.server.MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server != null ? server.registryAccess() : RegistryAccess.EMPTY;
    }

    public T createDefault(Object holder) {
        return defaultValueSupplier.apply(holder);
    }

    /** Creates and registers the backing Fabric attachment. */
    public synchronized void bind(Identifier id) {
        if (fabricType != null) {
            return;
        }
        net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry.Builder<T> builder = AttachmentRegistry.<T>builder();
        if (codec != null) {
            builder.persistent(codec);
        }
        if (copyOnDeath) {
            builder.copyOnDeath();
        }
        fabricType = builder.buildAndRegister(id);
    }

    public net.fabricmc.fabric.api.attachment.v1.AttachmentType<T> fabric() {
        return Objects.requireNonNull(fabricType, "Attachment type used before registration");
    }

    public static class Builder<T> {

        private final Function<Object, T> defaultValueSupplier;
        @Nullable
        private Codec<T> codec;
        private boolean copyOnDeath;

        private Builder(Function<Object, T> defaultValueSupplier) {
            this.defaultValueSupplier = defaultValueSupplier;
        }

        public Builder<T> serialize(Codec<T> codec) {
            this.codec = Objects.requireNonNull(codec);
            return this;
        }

        /** The "should serialize" predicate is not supported by Fabric attachments; values are always written. */
        public Builder<T> serialize(Codec<T> codec, Predicate<? super T> shouldSerialize) {
            return serialize(codec);
        }

        public Builder<T> copyOnDeath() {
            this.copyOnDeath = true;
            return this;
        }

        public Builder<T> copyHandler(IAttachmentCopyHandler<T> copyHandler) {
            //Fabric copies attachments marked copyOnDeath verbatim; custom copy handlers are approximated by copyOnDeath
            this.copyOnDeath = true;
            return this;
        }

        public Builder<T> sync(Object streamCodec) {
            return this;
        }

        public AttachmentType<T> build() {
            return new AttachmentType<>(this);
        }
    }
}
