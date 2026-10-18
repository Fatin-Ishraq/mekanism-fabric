package mekanism.neoshim.neoforge.attachment;

import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface IAttachmentCopyHandler<T> {

    @Nullable
    T copy(T attachment, IAttachmentHolder holder, HolderLookup.Provider provider);
}
