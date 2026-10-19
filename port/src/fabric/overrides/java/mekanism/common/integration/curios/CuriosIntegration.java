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

/**
 * FABRIC: Curios is NeoForge-only. On 1.21.1 this override was implemented on Trinkets (see the main branch); for the 26.1 port's first phase the accessory
 * integration is compiled out, so this stub reports no accessory slots. {@code MekanismHooks#curios} checks for "trinkets", which is not loaded.
 */
public class CuriosIntegration {

    /** An accessory slot holding a stack: the slot group/type id and the index within it. */
    public record AccessorySlot(String slotType, int index, ItemStack stack) {
    }

    public static void addListeners(IEventBus bus) {
    }

    @Nullable
    public static ResourceHandler<ItemResource> getCuriosInventory(LivingEntity entity) {
        return null;
    }

    public static Optional<AccessorySlot> findFirstCurioAsResult(LivingEntity entity, Predicate<ItemStack> filter) {
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
