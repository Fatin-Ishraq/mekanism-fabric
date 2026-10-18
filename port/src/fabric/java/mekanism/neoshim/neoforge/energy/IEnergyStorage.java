package mekanism.neoshim.neoforge.energy;

/** Forge Energy style storage. On Fabric it is bridged to Team Reborn Energy at the capability boundary (NOTES.md D4/D8). */
public interface IEnergyStorage {

    int receiveEnergy(int toReceive, boolean simulate);

    int extractEnergy(int toExtract, boolean simulate);

    int getEnergyStored();

    int getMaxEnergyStored();

    boolean canExtract();

    boolean canReceive();
}
