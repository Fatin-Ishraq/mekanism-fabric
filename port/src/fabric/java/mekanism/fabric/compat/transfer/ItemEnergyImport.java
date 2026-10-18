package mekanism.fabric.compat.transfer;

import java.util.function.ToIntFunction;
import mekanism.neoshim.neoforge.energy.IEnergyStorage;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

/** A foreign Team Reborn Energy item viewed as an {@link IEnergyStorage}; changes are written back into the stack in place. */
public final class ItemEnergyImport implements IEnergyStorage {

    private final ItemStack stack;

    private ItemEnergyImport(ItemStack stack) {
        this.stack = stack;
    }

    @Nullable
    public static ItemEnergyImport of(ItemStack stack) {
        return EnergyStorage.ITEM.find(stack, new ItemStackSlot(stack).context()) == null ? null : new ItemEnergyImport(stack);
    }

    private int run(ToIntFunction<IEnergyStorage> operation) {
        ItemStackSlot slot = new ItemStackSlot(stack);
        EnergyStorage storage = EnergyStorage.ITEM.find(stack, slot.context());
        if (storage == null) {
            return 0;
        }
        int result = operation.applyAsInt(new EnergyStorageAdapters.FromTeamReborn(storage));
        slot.writeBack();
        return result;
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        return run(storage -> storage.receiveEnergy(toReceive, simulate));
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        return run(storage -> storage.extractEnergy(toExtract, simulate));
    }

    @Override
    public int getEnergyStored() {
        return run(IEnergyStorage::getEnergyStored);
    }

    @Override
    public int getMaxEnergyStored() {
        return run(IEnergyStorage::getMaxEnergyStored);
    }

    @Override
    public boolean canExtract() {
        return run(storage -> storage.canExtract() ? 1 : 0) == 1;
    }

    @Override
    public boolean canReceive() {
        return run(storage -> storage.canReceive() ? 1 : 0) == 1;
    }
}
