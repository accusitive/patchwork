package party.stoat.patchwork.patchgraph.nodes;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.energy.IEnergyStorage;
import party.stoat.patchwork.Patchwork;
import party.stoat.patchwork.block.sf_controller.MultiEnergyHandler;
import party.stoat.patchwork.patchgraph.*;
import party.stoat.patchwork.block.sf_controller.SFControllerBlockEntity;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class SFSystemPowerNode extends Node {

    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath(Patchwork.MOD_ID, "patch_nodes/system_power");

    public SFSystemPowerNode(UUID uuid, NodeDescriptor descriptor) {
        super(uuid, descriptor);
    }

    @Override
    public void tick(StorageConfiguration config, PatchInstance patch, ServerLevel level, BlockGraph network, SFControllerBlockEntity controller) {
    }

    @Override
    public @Nullable IEnergyStorage getEnergyHandler(ServerLevel level, NodeDescriptor.IO port, PatchInstance graph) {
        var outputs = this.getOutputConnections(graph.graph);

        var handlers = outputs.stream().map(connection -> {
            if(connection == null) return null;

            var foreignNode = graph.nodes.get(connection.to());
            if(foreignNode == null) return null;

            return foreignNode.getEnergyHandler(level, foreignNode.getDescriptor().getPort(connection.keyTo()), graph);
        }).filter(Objects::nonNull);

        return new MultiEnergyHandler(handlers.toList());
    }

    @Override
    public ResourceLocation getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void acceptConfiguration(String string) {

    }

}
