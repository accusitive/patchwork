package party.stoat.patchwork.graphlib;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.util.NodePos;
import net.minecraft.server.level.ServerLevel;
import party.stoat.patchwork.Patchwork;

public class SFBehavior {

    public static void getPatchGraphs(BlockGraph graph) {

    }

    public static void networkUpdated(ServerLevel level, NodePos nodePos) {
        var graph = Patchwork.UNIVERSE.getGraphWorld(level).getGraphForNode(nodePos);
    }

}
