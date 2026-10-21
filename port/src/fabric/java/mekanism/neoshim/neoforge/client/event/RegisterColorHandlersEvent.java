package mekanism.neoshim.neoforge.client.event;

import com.mojang.serialization.MapCodec;
import java.util.List;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;

public abstract class RegisterColorHandlersEvent extends Event implements IModBusEvent {

    public static class BlockTintSources extends RegisterColorHandlersEvent {

        private final BlockColors blockColors;

        public BlockTintSources(BlockColors blockColors) {
            this.blockColors = blockColors;
        }

        public BlockColors getBlockColors() {
            return blockColors;
        }

        public void register(List<BlockTintSource> tintSources, Block... blocks) {
            blockColors.register(tintSources, blocks);
        }
    }

    public static class ItemTintSources extends RegisterColorHandlersEvent {

        private final ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends ItemTintSource>> idMapper;

        public ItemTintSources(ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends ItemTintSource>> idMapper) {
            this.idMapper = idMapper;
        }

        public void register(Identifier id, MapCodec<? extends ItemTintSource> codec) {
            idMapper.put(id, codec);
        }
    }
}
