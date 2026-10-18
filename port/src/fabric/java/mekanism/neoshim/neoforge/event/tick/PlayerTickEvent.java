package mekanism.neoshim.neoforge.event.tick;

import mekanism.neoshim.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.world.entity.player.Player;

public abstract class PlayerTickEvent extends PlayerEvent {

    protected PlayerTickEvent(Player player) {
        super(player);
    }

    public static class Pre extends PlayerTickEvent {

        public Pre(Player player) {
            super(player);
        }
    }

    public static class Post extends PlayerTickEvent {

        public Post(Player player) {
            super(player);
        }
    }
}
