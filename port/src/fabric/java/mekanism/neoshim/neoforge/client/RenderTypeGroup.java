package mekanism.neoshim.neoforge.client;

import net.minecraft.client.renderer.rendertype.RenderType;

public record RenderTypeGroup(RenderType block, RenderType entity, RenderType entityFabulous) {

    public static final RenderTypeGroup EMPTY = new RenderTypeGroup(null, null, null);

    public RenderTypeGroup(RenderType block, RenderType entity) {
        this(block, entity, entity);
    }

    public boolean isEmpty() {
        return block == null;
    }
}
