package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.common.Mekanism;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.TileEntityPersonalChest;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;

public class RenderPersonalChest extends ChestRenderer<TileEntityPersonalChest> {

    //nb: this is stitched by the Item's use of the texture
    private static final Identifier TEXTURE = Mekanism.rl("models/personal_chest");
    public static final SpriteId MATERIAL = Sheets.BLOCKS_MAPPER.apply(TEXTURE);

    public RenderPersonalChest(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    //FABRIC: NeoForge's getCustomSprite hook doesn't exist in vanilla 26.1, so replicate the vanilla submit with our sprite (models/sprites are access widened)
    @Override
    public void submit(final ChestRenderState state, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(modelTransformation(state.facing));
        float open = state.open;
        open = 1.0F - open;
        open = 1.0F - open * open * open;
        submitNodeCollector.submitModel(this.models.select(state.type), open, poseStack, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, MATERIAL, this.sprites, 0,
              state.breakProgress);
        poseStack.popPose();
    }

    //@Override//TODO - 26.1: Figure out if we need to setup profiling for this again?
    protected String getProfilerSection() {
        return ProfilerConstants.PERSONAL_CHEST;
    }
}
