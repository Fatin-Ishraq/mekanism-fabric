package mekanism.neoshim.neoforge.common.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;

/** NeoForge's FakePlayer, backed by Fabric API's FakePlayer. */
public class FakePlayer extends net.fabricmc.fabric.api.entity.FakePlayer {

    public FakePlayer(ServerLevel level, GameProfile name) {
        super(level, name);
    }
}
