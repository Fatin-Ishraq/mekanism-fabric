package mekanism.neoshim.neoforge.client.event;

import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.bus.api.ICancellableEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;

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

        private final FogEnvironment environment;
        private final FogType type;
        private final FogData fogData;

        public RenderFog(FogEnvironment environment, FogType type, Camera camera, float partialTicks, FogData fogData) {
            super(net.minecraft.client.Minecraft.getInstance().gameRenderer, camera, partialTicks);
            this.environment = environment;
            this.type = type;
            this.fogData = fogData;
        }

        public FogEnvironment getEnvironment() {
            return environment;
        }

        public FogType getType() {
            return type;
        }

        public FogData getFogData() {
            return fogData;
        }

        public float getFarPlaneDistance() {
            return fogData.environmentalEnd;
        }

        public float getNearPlaneDistance() {
            return fogData.environmentalStart;
        }

        public void setFarPlaneDistance(float distance) {
            fogData.environmentalEnd = distance;
        }

        public void setNearPlaneDistance(float distance) {
            fogData.environmentalStart = distance;
        }

        public void scaleFarPlaneDistance(float factor) {
            fogData.environmentalEnd *= factor;
        }

        public void scaleNearPlaneDistance(float factor) {
            fogData.environmentalStart *= factor;
        }
    }

    public static class ComputeFogColor extends ViewportEvent {

        private final Vector4f color;

        public ComputeFogColor(Camera camera, float partialTicks, Vector4f color) {
            super(net.minecraft.client.Minecraft.getInstance().gameRenderer, camera, partialTicks);
            this.color = color;
        }

        public Vector4f getColor() {
            return color;
        }

        public float getRed() {
            return color.x;
        }

        public void setRed(float red) {
            color.x = red;
        }

        public float getGreen() {
            return color.y;
        }

        public void setGreen(float green) {
            color.y = green;
        }

        public float getBlue() {
            return color.z;
        }

        public void setBlue(float blue) {
            color.z = blue;
        }
    }
}
