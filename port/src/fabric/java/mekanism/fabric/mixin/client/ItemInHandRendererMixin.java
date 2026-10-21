package mekanism.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.neoshim.neoforge.client.event.RenderArmEvent;
import mekanism.neoshim.neoforge.common.NeoForge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's RenderArmEvent (MekaSuit first-person arm rendering). */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Inject(method = "renderPlayerArm", at = @At("HEAD"), cancellable = true)
    private void mekanism$renderArm(PoseStack poseStack, SubmitNodeCollector collector, int light, float equipProgress, float swingProgress, HumanoidArm arm,
          CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && NeoForge.EVENT_BUS.post(new RenderArmEvent(poseStack, collector, light, minecraft.player, arm)).isCanceled()) {
            ci.cancel();
        }
    }
}
