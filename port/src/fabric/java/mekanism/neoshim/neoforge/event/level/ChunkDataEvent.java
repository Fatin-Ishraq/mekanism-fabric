package mekanism.neoshim.neoforge.event.level;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;

public abstract class ChunkDataEvent extends ChunkEvent<ChunkAccess> {

    private final CompoundTag data;

    protected ChunkDataEvent(ChunkAccess chunk, LevelAccessor level, CompoundTag data) {
        super(chunk, level);
        this.data = data;
    }

    public CompoundTag getData() {
        return data;
    }

    public static class Load extends ChunkDataEvent {

        public Load(ChunkAccess chunk, LevelAccessor level, CompoundTag data) {
            super(chunk, level, data);
        }
    }

    public static class Save extends ChunkDataEvent {

        public Save(ChunkAccess chunk, LevelAccessor level, CompoundTag data) {
            super(chunk, level, data);
        }
    }
}
