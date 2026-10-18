package mekanism.fabric.mixin;

import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LightEngine.class)
public interface LightEngineAccessor {

    @Accessor("chunkSource")
    LightChunkGetter mekanism$getChunkSource();

    @Invoker("enqueueIncrease")
    void mekanism$enqueueIncrease(long packedPos, long queueEntry);

    @Invoker("isEmptyShape")
    static boolean mekanism$isEmptyShape(BlockState state) {
        throw new AssertionError();
    }
}
