package mekanism.neoshim.neoforge.attachment;

import java.util.Optional;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import org.jetbrains.annotations.Nullable;

/** Injected into Entity, Level, ChunkAccess and BlockEntity (all Fabric {@link AttachmentTarget}s). */
public interface IAttachmentHolder {

    private AttachmentTarget mekanism$target() {
        return (AttachmentTarget) this;
    }

    default boolean hasData(AttachmentType<?> type) {
        return mekanism$target().hasAttached(type.fabric());
    }

    default <T> boolean hasData(Supplier<AttachmentType<T>> type) {
        return hasData(type.get());
    }

    default <T> T getData(AttachmentType<T> type) {
        return mekanism$target().getAttachedOrCreate(type.fabric(), () -> type.createDefault(this));
    }

    default <T> T getData(Supplier<AttachmentType<T>> type) {
        return getData(type.get());
    }

    default <T> Optional<T> getExistingData(AttachmentType<T> type) {
        return Optional.ofNullable(mekanism$target().getAttached(type.fabric()));
    }

    default <T> Optional<T> getExistingData(Supplier<AttachmentType<T>> type) {
        return getExistingData(type.get());
    }

    @Nullable
    default <T> T getExistingDataOrNull(AttachmentType<T> type) {
        return mekanism$target().getAttached(type.fabric());
    }

    @Nullable
    default <T> T getExistingDataOrNull(Supplier<AttachmentType<T>> type) {
        return getExistingDataOrNull(type.get());
    }

    @Nullable
    default <T> T setData(AttachmentType<T> type, T data) {
        return mekanism$target().setAttached(type.fabric(), data);
    }

    @Nullable
    default <T> T setData(Supplier<AttachmentType<T>> type, T data) {
        return setData(type.get(), data);
    }

    @Nullable
    default <T> T removeData(AttachmentType<T> type) {
        return mekanism$target().removeAttached(type.fabric());
    }

    @Nullable
    default <T> T removeData(Supplier<AttachmentType<T>> type) {
        return removeData(type.get());
    }
}
