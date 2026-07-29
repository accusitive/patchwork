package party.stoat.patchwork.patchgraph;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IMekanismChemicalHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import party.stoat.patchwork.block.sf_controller.SFControllerBlockEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public abstract class Node {

    public final UUID uuid;
    private final NodeDescriptor descriptor;

    public Node(UUID uuid, NodeDescriptor descriptor) {
        this.uuid = uuid;
        this.descriptor = descriptor;
    }

    public List<PatchGraph.Connection> getOutputConnections(PatchGraph graph) {
        return graph.connections.stream().filter(connection -> connection.from().equals(this.uuid)).toList();
    }

    public UUID getId() {
        return this.uuid;
    }

    public @Nullable IChemicalHandler getChemicalHandler(ServerLevel level, NodeDescriptor.IO port, PatchInstance graph) {
        return null;
    }

    public @Nullable IItemHandler getItemHandler(ServerLevel level, NodeDescriptor.IO port, PatchInstance graph) {
        return null;
    }

    public @Nullable IFluidHandler getFluidHandler(ServerLevel level, NodeDescriptor.IO port, PatchInstance graph) {
        return null;
    }

    public @Nullable EnergyStorage getEnergyHandler(ServerLevel level, NodeDescriptor.IO port, PatchInstance graph) {
        return null;
    }

    public void tick(StorageConfiguration config, PatchInstance patch, ServerLevel level, BlockGraph network, SFControllerBlockEntity controller) {

    }

    public NodeDescriptor getDescriptor() {
        return this.descriptor;
    }

    public abstract ResourceLocation getIdentifier();

    public abstract void acceptConfiguration(String string);

}
