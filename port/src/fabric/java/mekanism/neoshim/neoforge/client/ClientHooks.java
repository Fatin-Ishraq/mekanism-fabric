package mekanism.neoshim.neoforge.client;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.jetbrains.annotations.Nullable;

public class ClientHooks {

    /** @return true if a listener cancelled the default outline rendering. */
    public static boolean onDrawHighlight(net.minecraft.client.renderer.LevelRenderer context, net.minecraft.client.Camera camera,
          net.minecraft.world.phys.BlockHitResult target, net.minecraft.client.DeltaTracker deltaTracker, com.mojang.blaze3d.vertex.PoseStack poseStack,
          net.minecraft.client.renderer.MultiBufferSource bufferSource) {
        return mekanism.neoshim.neoforge.common.NeoForge.EVENT_BUS.post(new mekanism.neoshim.neoforge.client.event.RenderHighlightEvent.Block(context, camera, target,
              deltaTracker, poseStack, bufferSource)).isCanceled();
    }

    @Nullable
    public static SoundInstance playSound(SoundEngine manager, SoundInstance sound) {
        mekanism.neoshim.neoforge.client.event.sound.PlaySoundEvent event = new mekanism.neoshim.neoforge.client.event.sound.PlaySoundEvent(manager, sound);
        mekanism.neoshim.neoforge.common.NeoForge.EVENT_BUS.post(event);
        return event.getSound();
    }
}
