package mekanism.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.function.BiConsumer;
import mekanism.fabric.DynamicLightEmission;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Position-aware block light emission (NeoForge's BlockLightEngine patch). */
@Mixin(BlockLightEngine.class)
public abstract class BlockLightEngineMixin {

    @WrapOperation(method = "getEmission", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"))
    private int mekanism$positionalEmission(BlockState state, Operation<Integer> original, long packedPos, BlockState stateArg) {
        if (DynamicLightEmission.isDynamic(state.getBlock())) {
            BlockGetter level = ((LightEngineAccessor) this).mekanism$getChunkSource().getLevel();
            if (level != null) {
                return state.getBlock().getLightEmission(state, level, BlockPos.of(packedPos));
            }
        }
        return original.call(state);
    }

    @WrapOperation(method = "propagateLightSources", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LightChunk;findBlockLightSources(Ljava/util/function/BiConsumer;)V"))
    private void mekanism$positionalSources(LightChunk chunk, BiConsumer<BlockPos, BlockState> output, Operation<Void> original, ChunkPos chunkPos) {
        LightEngineAccessor engine = (LightEngineAccessor) this;
        original.call(chunk, (BiConsumer<BlockPos, BlockState>) (pos, state) -> {
            if (DynamicLightEmission.isDynamic(state.getBlock())) {
                int emission = state.getBlock().getLightEmission(state, chunk, pos);
                if (emission > 0) {
                    engine.mekanism$enqueueIncrease(pos.asLong(), LightEngine.QueueEntry.increaseLightFromEmission(emission, LightEngineAccessor.mekanism$isEmptyShape(state)));
                }
            } else {
                output.accept(pos, state);
            }
        });
    }
}
