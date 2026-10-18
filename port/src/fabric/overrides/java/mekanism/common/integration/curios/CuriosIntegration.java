package mekanism.common.integration.curios;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import mekanism.common.registries.MekanismItems;
import mekanism.neoshim.bus.api.IEventBus;
import mekanism.neoshim.fml.event.lifecycle.FMLClientSetupEvent;
import mekanism.neoshim.neoforge.items.IItemHandler;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * FABRIC override: Curios is NeoForge-only. The same entry points are implemented on Trinkets, the common Fabric accessory API (MekanismHooks#curios checks for
 * the {@code trinkets} mod on Fabric). Like upstream, no slots are assigned to Mekanism items here; modpacks/datapacks decide which items fit which trinket
 * slots. Slot ids are Trinkets' {@code group/slot} ids.
 */
public class CuriosIntegration {

    public static void addListeners(IEventBus bus) {
        bus.addListener((FMLClientSetupEvent event) -> mekanism.fabric.compat.trinkets.MekanismTrinketRenderer.register(MekanismItems.JETPACK, MekanismItems.ARMORED_JETPACK));
    }

    /** An occupied accessory slot: Trinkets slot id ({@code group/slot}) and index. */
    public record AccessorySlot(String slotType, int index, ItemStack stack) {
    }

    @Nullable
    public static IItemHandler getCuriosInventory(LivingEntity entity) {
        Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(entity);
        if (component.isEmpty()) {
            return null;
        }
        List<Container> inventories = new ArrayList<>();
        for (Map<String, TrinketInventory> group : component.get().getInventory().values()) {
            inventories.addAll(group.values());
        }
        return new ContainersItemHandler(inventories);
    }

    public static Optional<AccessorySlot> findFirstCurioSlot(@NotNull LivingEntity entity, Predicate<ItemStack> filter) {
        return TrinketsApi.getTrinketComponent(entity).flatMap(component -> {
            List<Tuple<SlotReference, ItemStack>> equipped = component.getEquipped(filter);
            if (equipped.isEmpty()) {
                return Optional.empty();
            }
            Tuple<SlotReference, ItemStack> first = equipped.getFirst();
            return Optional.of(new AccessorySlot(first.getA().inventory().getSlotType().getId(), first.getA().index(), first.getB()));
        });
    }

    public static ItemStack findFirstCurio(@NotNull LivingEntity entity, Predicate<ItemStack> filter) {
        return findFirstCurioSlot(entity, filter).map(AccessorySlot::stack).orElse(ItemStack.EMPTY);
    }

    public static ItemStack getCurioStack(@NotNull LivingEntity entity, String slotType, int slot) {
        int separator = slotType.indexOf('/');
        if (separator < 0) {
            return ItemStack.EMPTY;
        }
        return TrinketsApi.getTrinketComponent(entity).map(component -> {
            Map<String, TrinketInventory> group = component.getInventory().get(slotType.substring(0, separator));
            TrinketInventory inventory = group == null ? null : group.get(slotType.substring(separator + 1));
            return inventory == null || slot < 0 || slot >= inventory.getContainerSize() ? ItemStack.EMPTY : inventory.getItem(slot);
        }).orElse(ItemStack.EMPTY);
    }

    /** All trinket slots of an entity as one item handler (Mekanism uses it to charge and inspect worn accessories). */
    private record ContainersItemHandler(List<Container> containers) implements IItemHandler {

        @Nullable
        private Tuple<Container, Integer> locate(int slot) {
            for (Container container : containers) {
                if (slot < container.getContainerSize()) {
                    return new Tuple<>(container, slot);
                }
                slot -= container.getContainerSize();
            }
            return null;
        }

        @Override
        public int getSlots() {
            int slots = 0;
            for (Container container : containers) {
                slots += container.getContainerSize();
            }
            return slots;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            Tuple<Container, Integer> location = locate(slot);
            return location == null ? ItemStack.EMPTY : location.getA().getItem(location.getB());
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            Tuple<Container, Integer> location = locate(slot);
            if (stack.isEmpty() || location == null || !location.getA().getItem(location.getB()).isEmpty() || !location.getA().canPlaceItem(location.getB(), stack)) {
                return stack;
            }
            if (!simulate) {
                location.getA().setItem(location.getB(), stack.copyWithCount(1));
                location.getA().setChanged();
            }
            return stack.copyWithCount(stack.getCount() - 1);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            Tuple<Container, Integer> location = locate(slot);
            if (amount <= 0 || location == null) {
                return ItemStack.EMPTY;
            }
            ItemStack current = location.getA().getItem(location.getB());
            if (current.isEmpty()) {
                return ItemStack.EMPTY;
            }
            int extracted = Math.min(amount, current.getCount());
            if (simulate) {
                return current.copyWithCount(extracted);
            }
            ItemStack result = location.getA().removeItem(location.getB(), extracted);
            location.getA().setChanged();
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            Tuple<Container, Integer> location = locate(slot);
            return location == null ? 0 : location.getA().getMaxStackSize();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            Tuple<Container, Integer> location = locate(slot);
            return location != null && location.getA().canPlaceItem(location.getB(), stack);
        }
    }
}
