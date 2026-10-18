package mekanism.neoshim.neoforge.client.event;

import com.mojang.blaze3d.shaders.FogShape;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.bus.api.ICancellableEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;

public abstract class ViewportEvent extends Event {

    private final GameRenderer renderer;
    private final Camera camera;
    private final double partialTick;

    protected ViewportEvent(GameRenderer renderer, Camera camera, double partialTick) {
        this.renderer = renderer;
        this.camera = camera;
        this.partialTick = partialTick;
    }

    public GameRenderer getRenderer() {
        return renderer;
    }

    public Camera getCamera() {
        return camera;
    }

    public double getPartialTick() {
        return partialTick;
    }

    public static class RenderFog extends ViewportEvent implements ICancellableEvent {

        private final FogRenderer.FogMode mode;
        private final net.minecraft.world.level.material.FogType type;
        private float farPlaneDistance;
        private float nearPlaneDistance;
        private FogShape fogShape;

        public RenderFog(FogRenderer.FogMode mode, net.minecraft.world.level.material.FogType type, Camera camera, float partialTicks, float nearPlaneDistance, float farPlaneDistance, FogShape fogShape) {
            super(net.minecraft.client.Minecraft.getInstance().gameRenderer, camera, partialTicks);
            this.mode = mode;
            this.type = type;
            this.farPlaneDistance = farPlaneDistance;
            this.nearPlaneDistance = nearPlaneDistance;
            this.fogShape = fogShape;
        }

        public FogRenderer.FogMode getMode() {
            return mode;
        }

        public net.minecraft.world.level.material.FogType getType() {
            return type;
        }

        public float getFarPlaneDistance() {
            return farPlaneDistance;
        }

        public float getNearPlaneDistance() {
            return nearPlaneDistance;
        }

        public FogShape getFogShape() {
            return fogShape;
        }

        public void setFarPlaneDistance(float distance) {
            farPlaneDistance = distance;
        }

        public void setNearPlaneDistance(float distance) {
            nearPlaneDistance = distance;
        }

        public void setFogShape(FogShape shape) {
            fogShape = shape;
        }

        public void scaleFarPlaneDistance(float factor) {
            farPlaneDistance *= factor;
        }

        public void scaleNearPlaneDistance(float factor) {
            nearPlaneDistance *= factor;
        }
    }

    public static class ComputeFogColor extends ViewportEvent {

        private float red;
        private float green;
        private float blue;

        public ComputeFogColor(Camera camera, float partialTicks, float red, float green, float blue) {
            super(net.minecraft.client.Minecraft.getInstance().gameRenderer, camera, partialTicks);
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public float getRed() {
            return red;
        }

        public void setRed(float red) {
            this.red = red;
        }

        public float getGreen() {
            return green;
        }

        public void setGreen(float green) {
            this.green = green;
        }

        public float getBlue() {
            return blue;
        }

        public void setBlue(float blue) {
            this.blue = blue;
        }
    }
}
