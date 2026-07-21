package party.stoat.patchwork.block;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.util.NodePos;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;

public interface SFEnergyHandler extends EnergyHandler {

    int desiredAmount();

    void checkPowered(NodeHolder<BlockNode> node);

}
