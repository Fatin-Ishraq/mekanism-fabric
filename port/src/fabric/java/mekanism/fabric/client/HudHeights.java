package mekanism.fabric.client;

/** Stand-in for NeoForge's {@code Gui#leftHeight/rightHeight} (vanilla status bar stacking). Reset every frame by the HUD bridge. */
public final class HudHeights {

    public static int leftHeight = 39;
    public static int rightHeight = 39;

    private HudHeights() {
    }

    public static void reset() {
        leftHeight = 39;
        rightHeight = 39;
    }
}
