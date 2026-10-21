package mekanism.neoshim.neoforge.client.settings;

import net.minecraft.client.Minecraft;

public enum KeyModifier {
    CONTROL {
        @Override
        public boolean isActive(IKeyConflictContext conflictContext) {
            return Minecraft.getInstance().hasControlDown();
        }
    },
    SHIFT {
        @Override
        public boolean isActive(IKeyConflictContext conflictContext) {
            return Minecraft.getInstance().hasShiftDown();
        }
    },
    ALT {
        @Override
        public boolean isActive(IKeyConflictContext conflictContext) {
            return Minecraft.getInstance().hasAltDown();
        }
    },
    NONE {
        @Override
        public boolean isActive(IKeyConflictContext conflictContext) {
            return true;
        }
    };

    public abstract boolean isActive(IKeyConflictContext conflictContext);

    public static boolean isKeyCodeModifier(com.mojang.blaze3d.platform.InputConstants.Key key) {
        int code = key.getValue();
        return code == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL || code == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL
               || code == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT || code == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT
               || code == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT || code == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_ALT
               || code == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SUPER || code == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SUPER;
    }

    public static KeyModifier getActiveModifier() {
        for (KeyModifier modifier : values()) {
            if (modifier != NONE && modifier.isActive(null)) {
                return modifier;
            }
        }
        return NONE;
    }
}
