package mekanism.fabric.compat.transfer;

import mekanism.neoshim.neoforge.energy.IEnergyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import team.reborn.energy.api.EnergyStorage;

/** Forge-Energy style {@link IEnergyStorage} <-> Team Reborn Energy, 1 FE = 1 E (NOTES.md D8). */
public final class EnergyStorageAdapters {

    private EnergyStorageAdapters() {
    }

    /** Mekanism (FE view) exposed to TR Energy users. */
    public record ToTeamReborn(IEnergyStorage storage) implements EnergyStorage {

        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            int amount = (int) Math.min(Integer.MAX_VALUE, maxAmount);
            int accepted = storage.receiveEnergy(amount, true);
            if (accepted > 0) {
                transaction.addOuterCloseCallback(result -> {
                    if (result.wasCommitted()) {
                        storage.receiveEnergy(accepted, false);
                    }
                });
            }
            return accepted;
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            int amount = (int) Math.min(Integer.MAX_VALUE, maxAmount);
            int extracted = storage.extractEnergy(amount, true);
            if (extracted > 0) {
                transaction.addOuterCloseCallback(result -> {
                    if (result.wasCommitted()) {
                        storage.extractEnergy(extracted, false);
                    }
                });
            }
            return extracted;
        }

        @Override
        public long getAmount() {
            return storage.getEnergyStored();
        }

        @Override
        public long getCapacity() {
            return storage.getMaxEnergyStored();
        }

        @Override
        public boolean supportsInsertion() {
            return storage.canReceive();
        }

        @Override
        public boolean supportsExtraction() {
            return storage.canExtract();
        }
    }

    /** Foreign TR Energy storage viewed as an {@link IEnergyStorage}. */
    public record FromTeamReborn(EnergyStorage storage) implements IEnergyStorage {

        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            try (Transaction transaction = TransferUtil.open()) {
                long inserted = storage.insert(toReceive, transaction);
                if (!simulate) {
                    transaction.commit();
                }
                return (int) inserted;
            }
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            try (Transaction transaction = TransferUtil.open()) {
                long extracted = storage.extract(toExtract, transaction);
                if (!simulate) {
                    transaction.commit();
                }
                return (int) extracted;
            }
        }

        @Override
        public int getEnergyStored() {
            return (int) Math.min(Integer.MAX_VALUE, storage.getAmount());
        }

        @Override
        public int getMaxEnergyStored() {
            return (int) Math.min(Integer.MAX_VALUE, storage.getCapacity());
        }

        @Override
        public boolean canExtract() {
            return storage.supportsExtraction();
        }

        @Override
        public boolean canReceive() {
            return storage.supportsInsertion();
        }
    }
}
