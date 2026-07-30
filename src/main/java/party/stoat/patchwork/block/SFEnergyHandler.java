package party.stoat.patchwork.block;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import net.neoforged.neoforge.energy.IEnergyStorage;

public interface SFEnergyHandler extends IEnergyStorage {
    int desiredAmount();

    void checkPowered(NodeHolder<BlockNode> node);
}
