package mekanism.tools.client;

import mekanism.tools.client.render.item.RenderMekanismShieldItem.UnbakedShield;
import mekanism.tools.common.MekanismTools;
import mekanism.neoshim.api.distmarker.Dist;
import mekanism.neoshim.bus.api.SubscribeEvent;
import mekanism.neoshim.fml.common.EventBusSubscriber;
import mekanism.neoshim.neoforge.client.event.RegisterSpecialModelRendererEvent;
import mekanism.neoshim.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = MekanismTools.MODID, value = Dist.CLIENT)
public class ToolsClientRegistration {

    private ToolsClientRegistration() {
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        //TODO - 26.1: code updated to compile, but a singleton doesn't seem right? Probably should make a new instance for each?
        /*event.registerItem(new IClientItemExtensions() {
            @Override
            public Model getHumanoidArmorModel(ItemStack itemStack, EquipmentClientInfo.LayerType layerType, Model original) {
                if (!(original instanceof HumanoidModel<?> humanoidModel)) {
                    return original;
                }
                return GlowArmor.wrap(humanoidModel);
            }
        }, ToolsItems.REFINED_GLOWSTONE_HELMET, ToolsItems.REFINED_GLOWSTONE_CHESTPLATE, ToolsItems.REFINED_GLOWSTONE_LEGGINGS, ToolsItems.REFINED_GLOWSTONE_BOOTS);*/


    }

    @SubscribeEvent
    public static void registerSpecialRenderer(RegisterSpecialModelRendererEvent event) {
        event.register(UnbakedShield.ID, UnbakedShield.MAP_CODEC);
    }
}