package mekanism.neoshim.neoforge.client.event.sound;

import mekanism.neoshim.bus.api.Event;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.jetbrains.annotations.Nullable;

public class PlaySoundEvent extends Event {

    private final SoundEngine engine;
    private final String name;
    private final SoundInstance source;
    @Nullable
    private SoundInstance sound;

    public PlaySoundEvent(SoundEngine manager, SoundInstance sound) {
        this.engine = manager;
        this.source = sound;
        this.name = sound.getIdentifier().getPath();
        this.sound = sound;
    }

    public SoundEngine getEngine() {
        return engine;
    }

    public String getName() {
        return name;
    }

    public SoundInstance getOriginalSound() {
        return source;
    }

    @Nullable
    public SoundInstance getSound() {
        return sound;
    }

    public void setSound(@Nullable SoundInstance newSound) {
        this.sound = newSound;
    }
}
