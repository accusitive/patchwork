package party.stoat.patchwork.block.sf_controller;

import net.neoforged.neoforge.energy.EnergyStorage;

import java.util.ArrayList;
import java.util.List;

public class MultiEnergyHandler {

    public List<EnergyStorage> handlers;

    public MultiEnergyHandler(List<EnergyStorage> handlers) {
        this.handlers = handlers;
    }

    public List<EnergyStorage> getHandlers() {
        return this.handlers;
    }

    public long getAmountAsLong() {
        return getHandlers().stream().mapToLong(EnergyStorage::getEnergyStored).sum();
    }

    public long getCapacityAsLong() {
        return getHandlers().stream().mapToLong(EnergyStorage::getMaxEnergyStored).sum();
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
}
