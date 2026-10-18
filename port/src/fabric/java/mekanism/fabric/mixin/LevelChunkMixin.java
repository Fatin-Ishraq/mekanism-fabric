package mekanism.fabric.mixin;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** NeoForge applies chunk-packet block entity tags through {@code BlockEntity#handleUpdateTag}. */
@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {

    //Lambda inside replaceWithPacketData (synthetic name is stable: intermediary in both dev and production)
    @Redirect(method = "method_31716", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;loadWithComponents(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V"))
    private void mekanism$handleUpdateTag(BlockEntity blockEntity, CompoundTag tag, HolderLookup.Provider provider) {
        blockEntity.handleUpdateTag(tag, provider);
    }
}
