package mekanism.neoshim.neoforge.client;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

/** Set of chunk render layers (indexes into {@link ChunkSectionLayer#values()}). NeoForge's ChunkRenderTypeSet re-based on 26.1's layer enum. */
public final class ChunkRenderTypeSet implements Iterable<ChunkSectionLayer> {

    private static final List<ChunkSectionLayer> CHUNK_RENDER_TYPES_LIST = List.of(ChunkSectionLayer.values());
    private static final ChunkRenderTypeSet NONE = new ChunkRenderTypeSet(new BitSet());
    private static final ChunkRenderTypeSet ALL;

    static {
        BitSet bits = new BitSet();
        bits.set(0, CHUNK_RENDER_TYPES_LIST.size());
        ALL = new ChunkRenderTypeSet(bits);
    }

    public static ChunkRenderTypeSet none() {
        return NONE;
    }

    public static ChunkRenderTypeSet all() {
        return ALL;
    }

    public static ChunkRenderTypeSet of(ChunkSectionLayer... renderTypes) {
        return of(List.of(renderTypes));
    }

    public static ChunkRenderTypeSet of(Collection<ChunkSectionLayer> renderTypes) {
        BitSet bits = new BitSet();
        for (ChunkSectionLayer renderType : renderTypes) {
            int index = CHUNK_RENDER_TYPES_LIST.indexOf(renderType);
            if (index < 0) {
                throw new IllegalArgumentException("Attempted to create chunk render type set with a non-chunk render layer: " + renderType);
            }
            bits.set(index);
        }
        return new ChunkRenderTypeSet(bits);
    }

    public static ChunkRenderTypeSet union(ChunkRenderTypeSet... sets) {
        BitSet bits = new BitSet();
        for (ChunkRenderTypeSet set : sets) {
            bits.or(set.bits);
        }
        return new ChunkRenderTypeSet(bits);
    }

    public static ChunkRenderTypeSet union(Collection<ChunkRenderTypeSet> sets) {
        return union(sets.toArray(ChunkRenderTypeSet[]::new));
    }

    private final BitSet bits;

    private ChunkRenderTypeSet(BitSet bits) {
        this.bits = bits;
    }

    public boolean isEmpty() {
        return bits.isEmpty();
    }

    public boolean contains(ChunkSectionLayer renderType) {
        int index = CHUNK_RENDER_TYPES_LIST.indexOf(renderType);
        return index >= 0 && bits.get(index);
    }

    public List<ChunkSectionLayer> asList() {
        List<ChunkSectionLayer> list = new ArrayList<>();
        for (ChunkSectionLayer renderType : this) {
            list.add(renderType);
        }
        return list;
    }

    @Override
    public Iterator<ChunkSectionLayer> iterator() {
        return bits.stream().mapToObj(CHUNK_RENDER_TYPES_LIST::get).iterator();
    }
}
