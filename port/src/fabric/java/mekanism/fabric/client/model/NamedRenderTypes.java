package mekanism.fabric.client.model;

import java.util.Map;
import mekanism.neoshim.neoforge.client.RenderTypeGroup;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.resources.ResourceLocation;

/** NeoForge's named render types ("minecraft:solid", "minecraft:cutout", ...). */
public final class NamedRenderTypes {

    private static final Map<ResourceLocation, RenderTypeGroup> TYPES = Map.of(
          ResourceLocation.withDefaultNamespace("solid"), new RenderTypeGroup(RenderType.solid(), Sheets.solidBlockSheet()),
          ResourceLocation.withDefaultNamespace("cutout"), new RenderTypeGroup(RenderType.cutout(), Sheets.cutoutBlockSheet()),
          ResourceLocation.withDefaultNamespace("cutout_mipped"), new RenderTypeGroup(RenderType.cutoutMipped(), Sheets.cutoutBlockSheet()),
          ResourceLocation.withDefaultNamespace("translucent"), new RenderTypeGroup(RenderType.translucent(), Sheets.translucentCullBlockSheet()),
          ResourceLocation.withDefaultNamespace("tripwire"), new RenderTypeGroup(RenderType.tripwire(), Sheets.translucentCullBlockSheet())
    );

    private NamedRenderTypes() {
    }

    public static RenderTypeGroup get(ResourceLocation name) {
        return TYPES.getOrDefault(name, RenderTypeGroup.EMPTY);
    }
}
