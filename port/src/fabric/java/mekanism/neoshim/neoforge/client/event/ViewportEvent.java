package mekanism.neoshim.neoforge.client.event;

import mekanism.neoshim.bus.api.Event;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;

/** Camera/fog events. The fog events are fired by FogRendererMixin from vanilla's FogRenderer#setupFog. */
public abstract class ViewportEvent extends Event {

    private final GameRenderer renderer;
    private final Camera camera;
    private final double partialTick;

    public ViewportEvent(GameRenderer renderer, Camera camera, double partialTick) {
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

    /** Fired after the applicable fog environment set up the fog; near/far are the environmental fog start/end. */
    public static class RenderFog extends ViewportEvent {

        private final FogEnvironment environment;
        private final FogType type;
        private final FogData fogData;

        public RenderFog(FogEnvironment environment, FogType type, Camera camera, float partialTick, FogData fogData) {
            super(Minecraft.getInstance().gameRenderer, camera, partialTick);
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

        public float getFarPlaneDistance() {
            return fogData.environmentalEnd;
        }

        public float getNearPlaneDistance() {
            return fogData.environmentalStart;
        }

        public FogData getFogData() {
            return fogData;
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

    /** Fired after vanilla computed the fog colour; changes write through to the fog colour. */
    public static class ComputeFogColor extends ViewportEvent {

        private final Vector4f color;

        public ComputeFogColor(Camera camera, float partialTick, Vector4f color) {
            super(Minecraft.getInstance().gameRenderer, camera, partialTick);
            this.color = color;
        }

        public float getRed() {
            return color.x();
        }

        public void setRed(float red) {
            color.x = red;
        }

        public float getGreen() {
            return color.y();
        }

        public void setGreen(float green) {
            color.y = green;
        }

        public float getBlue() {
            return color.z();
        }

        public void setBlue(float blue) {
            color.z = blue;
        }
    }

    public static class ComputeCameraAngles extends ViewportEvent {

        private float yaw;
        private float pitch;
        private float roll;

        public ComputeCameraAngles(Camera camera, double partialTick, float yaw, float pitch, float roll) {
            super(Minecraft.getInstance().gameRenderer, camera, partialTick);
            this.yaw = yaw;
            this.pitch = pitch;
            this.roll = roll;
        }

        public float getYaw() {
            return yaw;
        }

        public void setYaw(float yaw) {
            this.yaw = yaw;
        }

        public float getPitch() {
            return pitch;
        }

        public void setPitch(float pitch) {
            this.pitch = pitch;
        }

        public float getRoll() {
            return roll;
        }

        public void setRoll(float roll) {
            this.roll = roll;
        }
    }

    public static class ComputeFov extends ViewportEvent {

        private final boolean usedConfiguredFov;
        private float fov;

        public ComputeFov(GameRenderer renderer, Camera camera, float partialTick, float fov, boolean usedConfiguredFov) {
            super(renderer, camera, partialTick);
            this.usedConfiguredFov = usedConfiguredFov;
            this.fov = fov;
        }

        public float getFOV() {
            return fov;
        }

        public void setFOV(float fov) {
            this.fov = fov;
        }

        public boolean usedConfiguredFov() {
            return usedConfiguredFov;
        }
    }
}
