package mekanism.fabric.mixin;

import java.util.function.BiConsumer;
import mekanism.fabric.DynamicLightEmission;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Blocks with position-aware light emission are light source candidates even when their state emits nothing. */
@Mixin(ChunkAccess.class)
public abstract class ChunkAccessLightMixin {

    @Shadow
    public abstract void findBlocks(java.util.function.Predicate<BlockState> predicate, BiConsumer<BlockPos, BlockState> output);

    @Inject(method = "findBlockLightSources", at = @At("HEAD"), cancellable = true)
    private void mekanism$dynamicSources(BiConsumer<BlockPos, BlockState> output, CallbackInfo ci) {
        findBlocks(state -> state.getLightEmission() != 0 || DynamicLightEmission.isDynamic(state.getBlock()), output);
        ci.cancel();
    }
}
