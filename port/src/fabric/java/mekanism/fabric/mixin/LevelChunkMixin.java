package mekanism.fabric.mixin;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** NeoForge applies chunk-packet block entity tags through {@code BlockEntity#handleUpdateTag}. */
@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {

    //Lambda inside replaceWithPacketData
    @Redirect(method = "lambda$replaceWithPacketData$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;loadWithComponents(Lnet/minecraft/world/level/storage/ValueInput;)V"))
    private void mekanism$handleUpdateTag(BlockEntity blockEntity, ValueInput input) {
        blockEntity.handleUpdateTag(input);
    }
}
