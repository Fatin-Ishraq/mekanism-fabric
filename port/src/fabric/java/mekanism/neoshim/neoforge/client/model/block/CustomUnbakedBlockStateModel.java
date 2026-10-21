package mekanism.neoshim.neoforge.client.model.block;

import com.mojang.serialization.MapCodec;

/** A block state model type selected by {@code type} in a blockstate JSON; backed by Fabric's equivalent so Fabric loads it. */
public interface CustomUnbakedBlockStateModel extends net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel {

    @Override
    MapCodec<? extends CustomUnbakedBlockStateModel> codec();
}
