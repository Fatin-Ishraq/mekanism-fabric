package mekanism.common.integration.curios;

import java.util.Optional;
import java.util.function.Predicate;
import mekanism.neoshim.bus.api.IEventBus;
import mekanism.neoshim.neoforge.transfer.ResourceHandler;
import mekanism.neoshim.neoforge.transfer.access.ItemAccess;
import mekanism.neoshim.neoforge.transfer.item.ItemResource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import top.theillusivec4.curios.api.SlotResult;

/// FABRIC stub: the Curios API is not available for Fabric 26.1; accessory support returns empty until an integration is restored (NOTES.md phase 9).
public class CuriosIntegration {

    public static void addListeners(IEventBus bus) {
    }

    @Nullable
    public static ResourceHandler<ItemResource> getCuriosInventory(LivingEntity entity) {
        return null;
    }

    public static Optional<SlotResult> findFirstCurioAsResult(LivingEntity entity, Predicate<ItemStack> filter) {
        return Optional.empty();
    }

    @Nullable
    public static ItemAccess findFirstCurio(LivingEntity entity, Predicate<ItemAccess> filter) {
        return null;
    }

    public static ItemStack getCurioStack(LivingEntity entity, String slotType, int slot) {
        return ItemStack.EMPTY;
    }
}
