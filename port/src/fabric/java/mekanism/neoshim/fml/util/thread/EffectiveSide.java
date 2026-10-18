package mekanism.neoshim.fml.util.thread;

import mekanism.fabric.FabricSides;
import mekanism.neoshim.fml.LogicalSide;

public class EffectiveSide {

    public static LogicalSide get() {
        return FabricSides.isServerThread(Thread.currentThread()) ? LogicalSide.SERVER : LogicalSide.CLIENT;
    }
}
