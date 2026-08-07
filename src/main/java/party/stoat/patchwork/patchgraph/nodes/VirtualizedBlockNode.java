package party.stoat.patchwork.patchgraph.nodes;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kneelawk.graphlib.api.graph.BlockGraph;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import party.stoat.patchwork.Patchwork;
import party.stoat.patchwork.compat.MekanismConfigurator;
import party.stoat.patchwork.patchgraph.*;
import party.stoat.patchwork.block.sf_controller.SFControllerBlockEntity;

import javax.annotation.Nullable;
import java.util.UUID;

public class VirtualizedBlockNode extends Node {

    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath(Patchwork.MOD_ID, "virtual");

    public BlockPos proxyPos;

    public VirtualizedBlockNode(UUID uuid, NodeDescriptor descriptor) {
        super(uuid, descriptor);
    }

    @Override
    public @Nullable IChemicalHandler getChemicalHandler(ServerLevel level, NodeDescriptor.IO port, PatchInstance graph) {
        if(this.proxyPos == null) return null;
        if(level == null) return null;
        return level.getCapability(mekanism.common.capabilities.Capabilities.CHEMICAL.block(), this.proxyPos, port.direction().orElse(null));
    }

    @Override
    public @Nullable IItemHandler getItemHandler(ServerLevel level, NodeDescriptor.IO port, PatchInstance graph) {
        if(this.proxyPos == null) return null;
        if(level == null) return null;
        return level.getCapability(Capabilities.ItemHandler.BLOCK, this.proxyPos, port.direction().orElse(null));
    }

    @Override
    public @Nullable IFluidHandler getFluidHandler(ServerLevel level, NodeDescriptor.IO port, PatchInstance graph) {
        if(this.proxyPos == null) return null;
        if(level == null) return null;
        return level.getCapability(Capabilities.FluidHandler.BLOCK, this.proxyPos, port.direction().orElse(null));
    }

    @Override
    public @Nullable IEnergyStorage getEnergyHandler(ServerLevel level, NodeDescriptor.IO port, PatchInstance graph) {
        if(this.proxyPos == null) return null;
        if(level == null) return null;
        return level.getCapability(Capabilities.EnergyStorage.BLOCK, this.proxyPos, port.direction().orElse(null));
    }

    @Override
    public void tick(StorageConfiguration config, PatchInstance patchInstance, ServerLevel level, BlockGraph network, SFControllerBlockEntity entity) {
        var outputs = this.getOutputConnections(patchInstance.graph);

        if (this.proxyPos == null) return;

        var descriptor = this.getDescriptor();

        for (var connection : outputs) {
            var connectedNode = patchInstance.nodes.get(connection.to());
            var port = descriptor.getPort(connection.keyFrom());
            if (connectedNode == null) continue;
            var foreignPort = connectedNode.getDescriptor().getPort(connection.keyTo());

            switch (port.d().d()) {
                case Chemical -> {
                    if (!ModList.get().isLoaded("mekanism")) continue;

                    var storage = getChemicalHandler(level, port, patchInstance);
                    var target = connectedNode.getChemicalHandler(level, foreignPort, patchInstance);

                    if (storage == null || target == null)
                        continue;

                    for (int sourceTank = 0; sourceTank < storage.getChemicalTanks(); sourceTank++) {
                        var chemical = storage.getChemicalInTank(sourceTank);

                        if (chemical.isEmpty())
                            continue;

                        // Simulate extraction
                        var simulatedExtract = storage.extractChemical(
                                chemical.getAmount(),
                                Action.SIMULATE
                        );

                        if (simulatedExtract.isEmpty())
                            continue;

                        // Simulate insertion
                        var simulatedRemainder = target.insertChemical(
                                simulatedExtract.copy(),
                                Action.SIMULATE
                        );

                        long accepted = simulatedExtract.getAmount() - simulatedRemainder.getAmount();

                        if (accepted <= 0)
                            continue;

                        // Actually extract exactly what the target accepted
                        var extracted = storage.extractChemical(
                                accepted,
                                Action.EXECUTE
                        );

                        if (extracted.isEmpty())
                            continue;

                        // Actually insert
                        var leftover = target.insertChemical(
                                extracted,
                                Action.EXECUTE
                        );

                        // Should not happen, but prevent loss if something changed
                        if (!leftover.isEmpty()) {
                            storage.insertChemical(leftover, Action.EXECUTE);
                        }
                    }
                }
                case Item -> {

                    var storage = getItemHandler(level, port, patchInstance);
                    var target = connectedNode.getItemHandler(level, foreignPort, patchInstance);

                    if (storage == null || target == null)
                        continue;

                    for (int sourceSlot = 0; sourceSlot < storage.getSlots(); sourceSlot++) {
                        var stack = storage.getStackInSlot(sourceSlot);

                        if (stack.isEmpty())
                            continue;

                        var simulatedExtract = storage.extractItem(sourceSlot, stack.getCount(), true);

                        if (simulatedExtract.isEmpty())
                            continue;

                        var remaining = simulatedExtract.copy();

                        for (int targetSlot = 0; targetSlot < target.getSlots() && !remaining.isEmpty(); targetSlot++) {
                            remaining = target.insertItem(targetSlot, remaining, true);
                        }

                        int accepted = simulatedExtract.getCount() - remaining.getCount();

                        if (accepted <= 0)
                            continue;

                        var extracted = storage.extractItem(sourceSlot, accepted, false);

                        if (extracted.isEmpty())
                            continue;

                        var leftover = extracted;

                        for (int targetSlot = 0; targetSlot < target.getSlots() && !leftover.isEmpty(); targetSlot++) {
                            leftover = target.insertItem(targetSlot, leftover, false);
                        }

                        if (!leftover.isEmpty()) {
                            for (int targetSlot = 0; targetSlot < storage.getSlots() && !leftover.isEmpty(); targetSlot++) {
                                leftover = storage.insertItem(targetSlot, leftover, false);
                            }
                        }
                    }
                }
                case Fluid -> {
                    var storage = getFluidHandler(level, port, patchInstance);
                    var target = connectedNode.getFluidHandler(level, foreignPort, patchInstance);

                    if (storage == null || target == null)
                        continue;

                    for (int sourceTank = 0; sourceTank < storage.getTanks(); sourceTank++) {
                        var fluid = storage.getFluidInTank(sourceTank);

                        if (fluid.isEmpty())
                            continue;

                        // Simulate extraction
                        var simulatedExtract = storage.drain(fluid, IFluidHandler.FluidAction.SIMULATE);

                        if (simulatedExtract.isEmpty())
                            continue;

                        // Simulate insertion into target
                        int accepted = 0;
                        var remaining = simulatedExtract.copy();

                        for (int targetTank = 0; targetTank < target.getTanks() && !remaining.isEmpty(); targetTank++) {
                            var inserted = target.fill(remaining, IFluidHandler.FluidAction.SIMULATE);

                            if (inserted > 0) {
                                accepted += inserted;
                                remaining.shrink(inserted);
                            }
                        }

                        if (accepted <= 0)
                            continue;

                        // Actually extract
                        var extracted = storage.drain(accepted, IFluidHandler.FluidAction.EXECUTE);

                        if (extracted.isEmpty())
                            continue;

                        // Actually insert
                        var leftover = extracted.copy();

                        for (int targetTank = 0; targetTank < target.getTanks() && !leftover.isEmpty(); targetTank++) {
                            int inserted = target.fill(leftover, IFluidHandler.FluidAction.EXECUTE);

                            if (inserted > 0)
                                leftover.shrink(inserted);
                        }

                        // Rollback if target changed unexpectedly
                        if (!leftover.isEmpty()) {
                            storage.fill(leftover, IFluidHandler.FluidAction.EXECUTE);
                        }
                    }
                }
                case Energy -> {
//                    var storage = level.getCapability(Capabilities.Energy.BLOCK, this.proxyPos, port.direction());
//
//                    if(storage != null) try(Transaction transaction = Transaction.open(context)) {
//
//                        try(Transaction inner = Transaction.open(transaction)) {
//                            var extracted = storage.extract( 1, inner);
//                            var foreignStorage = connectedNode.getItemHandler(level, foreignPort);
//                            var inserted = foreignStorage.insert(resource, extracted, inner);
//                            if(inserted == extracted) inner.commit();
//                        }
//
//                        transaction.commit();
//                    }
                }
            }
        }
    }

    @Override
    public ResourceLocation getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void acceptConfiguration(String string) {
        this.proxyPos = new Gson().fromJson(string, new TypeToken<BlockPos>() {}.getType());
    }

}
