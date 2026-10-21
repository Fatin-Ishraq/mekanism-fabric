package mekanism.neoshim.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.bus.api.ICancellableEvent;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.HumanoidArm;

public class RenderArmEvent extends Event implements ICancellableEvent {

    private final PoseStack poseStack;
    private final SubmitNodeCollector submitNodeCollector;
    private final int packedLight;
    private final AbstractClientPlayer player;
    private final HumanoidArm arm;

    public RenderArmEvent(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, AbstractClientPlayer player, HumanoidArm arm) {
        this.poseStack = poseStack;
        this.submitNodeCollector = submitNodeCollector;
        this.packedLight = packedLight;
        this.player = player;
        this.arm = arm;
    }

    public HumanoidArm getArm() {
        return arm;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public SubmitNodeCollector getSubmitNodeCollector() {
        return submitNodeCollector;
    }

    public int getPackedLight() {
        return packedLight;
    }

    public AbstractClientPlayer getPlayer() {
        return player;
    }
}
