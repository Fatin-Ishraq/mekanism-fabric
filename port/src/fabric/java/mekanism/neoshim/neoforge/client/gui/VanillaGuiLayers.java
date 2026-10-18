package mekanism.neoshim.neoforge.client.gui;

import net.minecraft.resources.Identifier;

public final class VanillaGuiLayers {

    public static final Identifier CAMERA_OVERLAYS = id("camera_overlays");
    public static final Identifier CROSSHAIR = id("crosshair");
    public static final Identifier HOTBAR = id("hotbar");
    public static final Identifier JUMP_METER = id("jump_meter");
    public static final Identifier EXPERIENCE_BAR = id("experience_bar");
    public static final Identifier PLAYER_HEALTH = id("player_health");
    public static final Identifier ARMOR_LEVEL = id("armor_level");
    public static final Identifier FOOD_LEVEL = id("food_level");
    public static final Identifier VEHICLE_HEALTH = id("vehicle_health");
    public static final Identifier AIR_LEVEL = id("air_level");
    public static final Identifier SELECTED_ITEM_NAME = id("selected_item_name");
    public static final Identifier SPECTATOR_TOOLTIP = id("spectator_tooltip");
    public static final Identifier EXPERIENCE_LEVEL = id("experience_level");
    public static final Identifier EFFECTS = id("effects");
    public static final Identifier BOSS_OVERLAY = id("boss_overlay");
    public static final Identifier SLEEP_OVERLAY = id("sleep_overlay");
    public static final Identifier DEMO_OVERLAY = id("demo_overlay");
    public static final Identifier DEBUG_OVERLAY = id("debug_overlay");
    public static final Identifier SCOREBOARD_SIDEBAR = id("scoreboard_sidebar");
    public static final Identifier OVERLAY_MESSAGE = id("overlay_message");
    public static final Identifier TITLE = id("title");
    public static final Identifier CHAT = id("chat");
    public static final Identifier TAB_LIST = id("tab_list");
    public static final Identifier SUBTITLE_OVERLAY = id("subtitle_overlay");
    public static final Identifier SAVING_INDICATOR = id("saving_indicator");

    private static Identifier id(String path) {
        return Identifier.withDefaultNamespace(path);
    }

    private VanillaGuiLayers() {
    }
}
