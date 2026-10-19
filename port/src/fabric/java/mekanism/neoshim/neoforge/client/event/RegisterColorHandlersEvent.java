package mekanism.neoshim.neoforge.client.event;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import java.util.List;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorResolverRegistry;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

/** Block tint sources go to Fabric's {@link BlockColorRegistry}, item tint source types to vanilla's id mapper, colour resolvers to Fabric. */
public abstract class RegisterColorHandlersEvent extends Event implements IModBusEvent {

    protected RegisterColorHandlersEvent() {
    }

    public static class BlockTintSources extends RegisterColorHandlersEvent {

        private final BlockColors blockColors;

        public BlockTintSources(BlockColors blockColors) {
            this.blockColors = blockColors;
        }

        public BlockColors getBlockColors() {
            return blockColors;
        }

        public void register(List<BlockTintSource> sources, Block... blocks) {
            BlockColorRegistry.register(sources, blocks);
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

    public static class ColorResolvers extends RegisterColorHandlersEvent {

        @Nullable
        private final ImmutableList.Builder<ColorResolver> builder;

        public ColorResolvers(@Nullable ImmutableList.Builder<ColorResolver> builder) {
            this.builder = builder;
        }

        public void register(ColorResolver resolver) {
            if (builder != null) {
                builder.add(resolver);
            }
            ColorResolverRegistry.register(resolver);
        }
    }
}
