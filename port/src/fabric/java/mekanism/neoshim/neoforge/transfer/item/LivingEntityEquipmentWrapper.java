package mekanism.neoshim.neoforge.transfer.item;

import java.util.Arrays;
import java.util.List;
import mekanism.neoshim.neoforge.transfer.ResourceHandler;
import mekanism.neoshim.neoforge.transfer.TransferPreconditions;
import mekanism.neoshim.neoforge.transfer.transaction.SnapshotJournal;
import mekanism.neoshim.neoforge.transfer.transaction.TransactionContext;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** A living entity's equipment slots as a resource handler. */
public class LivingEntityEquipmentWrapper {

    public static ResourceHandler<ItemResource> of(LivingEntity entity, EquipmentSlot.Type type) {
        return new Handler(entity, Arrays.stream(EquipmentSlot.values()).filter(slot -> slot.getType() == type).toList());
    }

    public static ResourceHandler<ItemResource> of(LivingEntity entity, EquipmentSlot slot) {
        return new Handler(entity, List.of(slot));
    }

    private static final class Handler implements ResourceHandler<ItemResource> {

        private final LivingEntity entity;
        private final List<EquipmentSlot> slots;
        private final List<SlotJournal> journals;

        private Handler(LivingEntity entity, List<EquipmentSlot> slots) {
            this.entity = entity;
            this.slots = slots;
            this.journals = slots.stream().map(SlotJournal::new).toList();
        }

        @Override
        public int size() {
            return slots.size();
        }

        @Override
        public ItemResource getResource(int index) {
            return ItemResource.of(entity.getItemBySlot(slots.get(index)));
        }

        @Override
        public long getAmountAsLong(int index) {
            return entity.getItemBySlot(slots.get(index)).getCount();
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return resource.isEmpty() ? 99 : resource.getMaxStackSize();
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return true;
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            ItemStack current = entity.getItemBySlot(slots.get(index));
            if (!current.isEmpty() && !resource.matches(current)) {
                return 0;
            }
            int inserted = Math.min(amount, resource.getMaxStackSize() - current.getCount());
            if (inserted <= 0) {
                return 0;
            }
            journals.get(index).updateSnapshots(transaction);
            entity.setItemSlot(slots.get(index), resource.toStack(current.getCount() + inserted));
            return inserted;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            ItemStack current = entity.getItemBySlot(slots.get(index));
            if (current.isEmpty() || !resource.matches(current)) {
                return 0;
            }
            int extracted = Math.min(amount, current.getCount());
            journals.get(index).updateSnapshots(transaction);
            entity.setItemSlot(slots.get(index), current.copyWithCount(current.getCount() - extracted));
            return extracted;
        }

        private final class SlotJournal extends SnapshotJournal<ItemStack> {

            private final EquipmentSlot slot;

            private SlotJournal(EquipmentSlot slot) {
                this.slot = slot;
            }

            @Override
            protected ItemStack createSnapshot() {
                return entity.getItemBySlot(slot).copy();
            }

            @Override
            protected void revertToSnapshot(ItemStack snapshot) {
                entity.setItemSlot(slot, snapshot);
            }
        }
    }
}
