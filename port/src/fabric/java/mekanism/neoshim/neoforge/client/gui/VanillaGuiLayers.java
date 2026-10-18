package mekanism.neoshim.neoforge.client.gui;

import net.minecraft.resources.ResourceLocation;

public final class VanillaGuiLayers {

    public static final ResourceLocation CAMERA_OVERLAYS = id("camera_overlays");
    public static final ResourceLocation CROSSHAIR = id("crosshair");
    public static final ResourceLocation HOTBAR = id("hotbar");
    public static final ResourceLocation JUMP_METER = id("jump_meter");
    public static final ResourceLocation EXPERIENCE_BAR = id("experience_bar");
    public static final ResourceLocation PLAYER_HEALTH = id("player_health");
    public static final ResourceLocation ARMOR_LEVEL = id("armor_level");
    public static final ResourceLocation FOOD_LEVEL = id("food_level");
    public static final ResourceLocation VEHICLE_HEALTH = id("vehicle_health");
    public static final ResourceLocation AIR_LEVEL = id("air_level");
    public static final ResourceLocation SELECTED_ITEM_NAME = id("selected_item_name");
    public static final ResourceLocation SPECTATOR_TOOLTIP = id("spectator_tooltip");
    public static final ResourceLocation EXPERIENCE_LEVEL = id("experience_level");
    public static final ResourceLocation EFFECTS = id("effects");
    public static final ResourceLocation BOSS_OVERLAY = id("boss_overlay");
    public static final ResourceLocation SLEEP_OVERLAY = id("sleep_overlay");
    public static final ResourceLocation DEMO_OVERLAY = id("demo_overlay");
    public static final ResourceLocation DEBUG_OVERLAY = id("debug_overlay");
    public static final ResourceLocation SCOREBOARD_SIDEBAR = id("scoreboard_sidebar");
    public static final ResourceLocation OVERLAY_MESSAGE = id("overlay_message");
    public static final ResourceLocation TITLE = id("title");
    public static final ResourceLocation CHAT = id("chat");
    public static final ResourceLocation TAB_LIST = id("tab_list");
    public static final ResourceLocation SUBTITLE_OVERLAY = id("subtitle_overlay");
    public static final ResourceLocation SAVING_INDICATOR = id("saving_indicator");

    private static ResourceLocation id(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }

    private VanillaGuiLayers() {
    }
}
