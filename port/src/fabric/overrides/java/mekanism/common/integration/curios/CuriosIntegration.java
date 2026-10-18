package mekanism.common.integration.curios;

import java.util.function.Predicate;
import mekanism.neoshim.bus.api.IEventBus;
import mekanism.neoshim.neoforge.items.IItemHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * FABRIC override: Curios is NeoForge-only, so this is a no-op stand-in with the same entry points. Accessory slot support on Fabric
 * (Trinkets/Accessories) is a phase-9 TODO (NOTES.md "Stubbed / disabled").
 */
public class CuriosIntegration {

    public static void addListeners(IEventBus bus) {
    }

    @Nullable
    public static IItemHandler getCuriosInventory(LivingEntity entity) {
        return null;
    }

    public static ItemStack findFirstCurio(@NotNull LivingEntity entity, Predicate<ItemStack> filter) {
        return ItemStack.EMPTY;
    }

    public static ItemStack getCurioStack(@NotNull LivingEntity entity, String slotType, int slot) {
        return ItemStack.EMPTY;
    }
}
