package party.stoat.patchwork.block.sf_controller;

import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.ArrayList;
import java.util.List;

public class MultiEnergyHandler implements IEnergyStorage {

    public List<IEnergyStorage> handlers;

    public MultiEnergyHandler(List<IEnergyStorage> handlers) {
        this.handlers = handlers;
    }

    public List<IEnergyStorage> getHandlers() {
        return this.handlers;
    }

    public long getAmountAsLong() {
        return getHandlers().stream().mapToLong(IEnergyStorage::getEnergyStored).sum();
    }

    public long getCapacityAsLong() {
        return getHandlers().stream().mapToLong(IEnergyStorage::getMaxEnergyStored).sum();
    }

    public int insert(int amount, boolean simulate) {
        if(getHandlers().isEmpty()) return 0;

        long surplus = 0;

        long per = amount / getHandlers().size();

        long totalInserted = 0;

        for(var handler : getHandlers()) {
            var inserted = handler.receiveEnergy((int)per, simulate);
            totalInserted += inserted;
            surplus += per - inserted;
        }

        surplus += amount - totalInserted;
        surplus = Math.min(surplus, Integer.MAX_VALUE);

        for(var handler : getHandlers()) {
            if(surplus < 0) break;
            var surplusInserted = handler.receiveEnergy((int)surplus, simulate);
            surplus -= surplusInserted;
            totalInserted += surplusInserted;
            if(surplus == 0) break;
        }

        return (int) Math.min(totalInserted, Integer.MAX_VALUE);
    }

    public int extract(int amount, boolean simulate) {
        return 0;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return insert(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return extract(maxExtract, simulate);
    }

    @Override
    public int getEnergyStored() {
        return (int)Math.min(getAmountAsLong(), Integer.MAX_VALUE);
    }

    @Override
    public int getMaxEnergyStored() {
        return (int)Math.min(getCapacityAsLong(), Integer.MAX_VALUE);
    }

    @Override
    public boolean canExtract() {
        return handlers.stream().anyMatch(IEnergyStorage::canExtract);
    }

    @Override
    public boolean canReceive() {
        return handlers.stream().anyMatch(IEnergyStorage::canReceive);
    }
}
