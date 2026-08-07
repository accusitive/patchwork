package party.stoat.patchwork.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import party.stoat.patchwork.Patchwork;
import party.stoat.patchwork.client.screen.EditorScreen;
import party.stoat.patchwork.patchgraph.NodeDescriptor;
import party.stoat.patchwork.patchgraph.PatchGraph;
import party.stoat.patchwork.patchgraph.StorageConfiguration;

import java.util.*;

public record SFControllerSyncClientboundPayload(List<PatchGraph> patches, List<StorageConfiguration.NodeCategory> nodeDescriptors, BlockPos controllerPos, Optional<UUID> view) implements CustomPacketPayload {

    public static final ResourceLocation PATCH_CONTROLLER_SYNC = ResourceLocation.fromNamespaceAndPath(Patchwork.MOD_ID, "patch_sync");
    public static final CustomPacketPayload.Type<SFControllerSyncClientboundPayload> TYPE = new CustomPacketPayload.Type<>(PATCH_CONTROLLER_SYNC);
    public static final StreamCodec<RegistryFriendlyByteBuf, SFControllerSyncClientboundPayload> CODEC = StreamCodec
            .composite(
                    ByteBufCodecs.fromCodec(PatchGraph.CODEC).apply(ByteBufCodecs.list()), SFControllerSyncClientboundPayload::patches,
                    ByteBufCodecs.fromCodec(StorageConfiguration.NodeCategory.CODEC).apply(ByteBufCodecs.list()), SFControllerSyncClientboundPayload::nodeDescriptors,
                    BlockPos.STREAM_CODEC, SFControllerSyncClientboundPayload::controllerPos,
                    ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), SFControllerSyncClientboundPayload::view,
                    SFControllerSyncClientboundPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SFControllerSyncClientboundPayload payload, IPayloadContext context) {
        var mc = Minecraft.getInstance();

        if(mc.screen instanceof EditorScreen editor) {
            UUID currentGraphId = null;
            HashMap<UUID, Vec2> oldPositions = null;

            editor.state.patchGraphs = new ArrayList<>(payload.patches());
            editor.state.serverProvidedDescriptors = new ArrayList<>(payload.nodeDescriptors());

            if(editor.state.getCurrentGraph() != null) {
                currentGraphId = editor.state.getCurrentGraph().graphId;
                oldPositions = editor.state.getCurrentGraph().nodePositions;
            } else {
                payload.view().ifPresent(id -> {
                    var current = payload.patches().stream().filter(p -> p.graphId.equals(id)).findFirst();
                    current.ifPresent(editor::setGraph);
                });
            }

            editor.state.controllerPos = payload.controllerPos();

            if(editor.state.getCurrentGraph() == null && !editor.state.patchGraphs.isEmpty()) editor.setGraph(editor.state.patchGraphs.get(0));

            if(currentGraphId != null) {
                for(var graph : editor.state.patchGraphs) if(graph.graphId.equals(currentGraphId)) editor.setGraph(graph);
            }

            if(oldPositions != null && editor.state.getCurrentGraph() != null) {
                editor.state.getCurrentGraph().nodePositions.putAll(oldPositions);
            }

            editor.state.editorDirty = false;
            editor.refresh(mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
        }
    }
}
