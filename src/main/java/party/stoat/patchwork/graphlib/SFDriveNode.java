package party.stoat.patchwork.graphlib;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.BlockNodeType;
import com.kneelawk.graphlib.api.wire.FullWireBlockNode;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import party.stoat.patchwork.Patchwork;

public class SFDriveNode implements FullWireBlockNode {

    public static final SFDriveNode INSTANCE = new SFDriveNode();
    public static final BlockNodeType TYPE = BlockNodeType.of(Identifier.fromNamespaceAndPath(Patchwork.MOD_ID, "sf_drive"), () -> INSTANCE);

    @Override
    public @NotNull BlockNodeType getType() {
        return TYPE;
    }

    @Override
    public void onConnectionsChanged(@NotNull NodeHolder<BlockNode> self) {

    }

}
