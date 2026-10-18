package mekanism.fabric.client;

import mekanism.neoshim.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import mekanism.neoshim.neoforge.fluids.FluidType;
import net.minecraft.resources.ResourceLocation;

/** Client fluid extensions for vanilla water/lava (NeoForge provides these itself). */
public final class VanillaFluidClientExtensions {

    private static final IClientFluidTypeExtensions WATER = new IClientFluidTypeExtensions() {
        @Override
        public ResourceLocation getStillTexture() {
            return ResourceLocation.withDefaultNamespace("block/water_still");
        }

        @Override
        public ResourceLocation getFlowingTexture() {
            return ResourceLocation.withDefaultNamespace("block/water_flow");
        }

        @Override
        public ResourceLocation getOverlayTexture() {
            return ResourceLocation.withDefaultNamespace("block/water_overlay");
        }

        @Override
        public int getTintColor() {
            return 0xFF3F76E4;
        }
    };
    private static final IClientFluidTypeExtensions LAVA = new IClientFluidTypeExtensions() {
        @Override
        public ResourceLocation getStillTexture() {
            return ResourceLocation.withDefaultNamespace("block/lava_still");
        }

        @Override
        public ResourceLocation getFlowingTexture() {
            return ResourceLocation.withDefaultNamespace("block/lava_flow");
        }
    };

    private VanillaFluidClientExtensions() {
    }

    public static IClientFluidTypeExtensions get(FluidType type) {
        if (type == FluidType.VanillaTypes.WATER) {
            return WATER;
        } else if (type == FluidType.VanillaTypes.LAVA) {
            return LAVA;
        }
        return IClientFluidTypeExtensions.DEFAULT;
    }
}
