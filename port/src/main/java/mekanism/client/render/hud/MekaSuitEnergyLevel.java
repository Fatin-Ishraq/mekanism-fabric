package mekanism.client.render.hud;

import mekanism.api.math.MathUtils;
import mekanism.client.gui.element.bar.GuiBar;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import mekanism.neoshim.neoforge.client.gui.GuiLayer;
import mekanism.neoshim.neoforge.transfer.ResourceHandler;
import mekanism.neoshim.neoforge.transfer.access.ItemAccess;
import mekanism.neoshim.neoforge.transfer.energy.EnergyHandler;
import mekanism.neoshim.neoforge.transfer.item.ItemResource;
import mekanism.neoshim.neoforge.transfer.item.LivingEntityEquipmentWrapper;

public class MekaSuitEnergyLevel implements GuiLayer {

    public static final MekaSuitEnergyLevel INSTANCE = new MekaSuitEnergyLevel();
    private static final Identifier POWER_BAR = MekanismUtils.getResource(ResourceType.GUI_BAR, "horizontal_power_long.png");

    private MekaSuitEnergyLevel() {
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.gameMode.canHurtPlayer() || minecraft.options.hideGui || !MekanismConfig.client.hudRenderMekaSuitEnergyBar.get()) {
            //canHurtPlayer is a copy of vanilla check for if the armor level can render
            return;
        }
        long capacity = 0L, stored = 0L;
        ResourceHandler<ItemResource> armorSlots = LivingEntityEquipmentWrapper.of(minecraft.player, EquipmentSlot.Type.HUMANOID_ARMOR);
        for (int slot = 0, size = armorSlots.size(); slot < size; slot++) {
            ItemResource itemType = armorSlots.getResource(slot);
            if (!itemType.isEmpty() && itemType.value() instanceof ItemMekaSuitArmor) {
                EnergyHandler energyHandler = Capabilities.ENERGY.getCapability(ItemAccess.forHandlerIndexStrict(armorSlots, slot));
                if (energyHandler != null) {
                    capacity = MathUtils.addClamped(capacity, energyHandler.getCapacityAsLong());
                    stored = MathUtils.addClamped(stored, energyHandler.getAmountAsLong());
                }
            }
        }
        if (capacity > 0) {
            int x = graphics.guiWidth() / 2 - 91;
            int y = graphics.guiHeight() - mekanism.fabric.client.HudHeights.leftHeight + 2;//FABRIC: Gui#leftHeight is a NeoForge field
            int length = (int) Math.round(((double) stored / capacity) * 79);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, GuiBar.BAR, x, y, 81, 6);
            graphics.blit(RenderPipelines.GUI_TEXTURED, POWER_BAR, x + 1, y + 1, 0, 0, length, 4, length, 4, 79, 4);
            mekanism.fabric.client.HudHeights.leftHeight += 8;//FABRIC: Gui#leftHeight is a NeoForge field
        }
    }
}