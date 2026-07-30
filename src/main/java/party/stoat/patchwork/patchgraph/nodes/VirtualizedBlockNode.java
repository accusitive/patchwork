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

    private static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath(Patchwork.MOD_ID, "patch_nodes/container_node");

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
                    var foreignStorage = connectedNode.getChemicalHandler(level, foreignPort, patchInstance);
                    if (storage == null || foreignStorage == null) continue;

                    for (int localIndex = 0; localIndex < storage.getChemicalTanks(); localIndex++) {
                        ChemicalStack resource = storage.getChemicalInTank(localIndex);
                        if (resource.isEmpty()) continue;

                        for (int foreignIndex = 0; foreignIndex < foreignStorage.getChemicalTanks(); foreignIndex++) {
                            // Simulate extraction
                            ChemicalStack extracted = storage.extractChemical(localIndex, resource.getAmount(), Action.SIMULATE);

                            if (extracted.isEmpty()) continue;

                            // Simulate insertion
                            ChemicalStack remainder = foreignStorage.insertChemical(foreignIndex, extracted, Action.SIMULATE);
                            long insertedAmount = extracted.getAmount() - remainder.getAmount();
                            if (insertedAmount <= 0) continue;

                            // Extract only what can fit
                            ChemicalStack toTransfer = extracted.copy();
                            toTransfer.setAmount(insertedAmount);

                            // Execute extraction
                            ChemicalStack actualExtracted = storage.extractChemical(localIndex, insertedAmount, Action.EXECUTE);
                            if (actualExtracted.isEmpty())
                                continue;

                            // Execute insertion
                            ChemicalStack insertRemainder = foreignStorage.insertChemical(foreignIndex, actualExtracted, Action.EXECUTE);
                            // Normally this should be empty unless the machine changed between calls
                            if (!insertRemainder.isEmpty()) {
                                // optional: handle failed remainder
                            }

                            break;
                        }
                    }
                }
                case Item -> {

                    var storage = getItemHandler(level, port, patchInstance);

                    var target = connectedNode.getItemHandler(level, foreignPort, patchInstance);

                    if (storage == null || target == null)
                        continue;


                    for (int slot = 0; slot < storage.getSlots(); slot++) {

                        var stack = storage.getStackInSlot(slot);

                        if (stack.isEmpty())
                            continue;


                        // simulate insertion
                        var remainder = target.insertItem(0, stack, true);

                        int amount = stack.getCount() - remainder.getCount();

                        if (amount <= 0)
                            continue;


                        // actually extract
                        var extracted = storage.extractItem(slot, amount, false);

                        if (extracted.isEmpty())
                            continue;


                        // actually insert
                        target.insertItem(0, extracted, false);
                    }
                }
                case Fluid -> {

                    var storage = getFluidHandler(level, port, patchInstance);
                    var target = connectedNode.getFluidHandler(level, foreignPort, patchInstance);

                    if(storage == null || target == null)
                        continue;


                    for(int tank = 0; tank < storage.getTanks(); tank++) {

                        var fluid = storage.getFluidInTank(tank);

                        if(fluid.isEmpty())
                            continue;


                        var simulated = fluid.copy();

                        int accepted =
                                target.fill(simulated, IFluidHandler.FluidAction.SIMULATE);


                        if(accepted <= 0)
                            continue;


                        var extracted =
                                storage.drain(
                                        fluid.copyWithAmount(accepted),
                                        IFluidHandler.FluidAction.EXECUTE
                                );


                        if(!extracted.isEmpty()) {
                            target.fill(
                                    extracted,
                                    IFluidHandler.FluidAction.EXECUTE
                            );
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
