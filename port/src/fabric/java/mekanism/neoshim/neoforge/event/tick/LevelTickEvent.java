package mekanism.neoshim.neoforge.event.tick;

import java.util.function.BooleanSupplier;
import mekanism.neoshim.bus.api.Event;
import net.minecraft.world.level.Level;

public abstract class LevelTickEvent extends Event {

    private final BooleanSupplier hasTime;
    private final Level level;

    protected LevelTickEvent(BooleanSupplier hasTime, Level level) {
        this.hasTime = hasTime;
        this.level = level;
    }

    public boolean hasTime() {
        return hasTime.getAsBoolean();
    }

    public Level getLevel() {
        return level;
    }

    public static class Pre extends LevelTickEvent {

        public Pre(BooleanSupplier hasTime, Level level) {
            super(hasTime, level);
        }
    }

    public static class Post extends LevelTickEvent {

        public Post(BooleanSupplier hasTime, Level level) {
            super(hasTime, level);
        }
    }
}
