package mekanism.neoshim.neoforge.event.level;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;

public abstract class ChunkEvent<T extends ChunkAccess> extends LevelEvent {

    private final T chunk;

    protected ChunkEvent(T chunk, LevelAccessor level) {
        super(level);
        this.chunk = chunk;
    }

    public T getChunk() {
        return chunk;
    }

    public static class Load extends ChunkEvent<LevelChunk> {

        private final boolean newChunk;

        public Load(LevelChunk chunk, boolean newChunk) {
            super(chunk, chunk.getLevel());
            this.newChunk = newChunk;
        }

        public boolean isNewChunk() {
            return newChunk;
        }
    }

    public static class Unload extends ChunkEvent<LevelChunk> {

        public Unload(LevelChunk chunk) {
            super(chunk, chunk.getLevel());
        }
    }
}
