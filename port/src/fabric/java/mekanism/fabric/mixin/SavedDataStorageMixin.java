package mekanism.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.DataFixer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** NeoForge allows saved data types without a data fixer type ({@code SavedDataType} with a null {@link DataFixTypes}): such data is read unchanged. */
@Mixin(SavedDataStorage.class)
public abstract class SavedDataStorageMixin {

    @WrapOperation(method = "readTagFromDisk", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/datafix/DataFixTypes;update(Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/nbt/CompoundTag;II)Lnet/minecraft/nbt/CompoundTag;"))
    private CompoundTag mekanism$skipNullFixer(DataFixTypes type, DataFixer fixer, CompoundTag tag, int version, int newVersion, Operation<CompoundTag> original) {
        return type == null ? tag : original.call(type, fixer, tag, version, newVersion);
    }
}
