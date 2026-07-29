package party.stoat.patchwork.client.screen.components;

import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3x2f;
import party.stoat.patchwork.client.screen.EditorScreen;
import party.stoat.patchwork.patchgraph.PatchGraph;
import party.stoat.patchwork.patchgraph.NodeDescriptor;

import java.util.List;
import java.util.UUID;

public class NodeIOPort extends Renderable {

    public String key;
    public UUID owner;

    public NodeDescriptor.DataType type;

    public NodeIOPort(String key, UUID node, NodeDescriptor.DataType type) {
        this.key = key;
        this.owner = node;
        this.type = type;
    }

    @Override
    protected Layout extractInnerLayout(int x, int y, int z) {
        return new Layout(x - 2, y - 2, 0, 8, 8, this, List.of(), false);
    }

    @Override
    public void paint(GuiGraphics g, Layout l) {
        super.paint(g, l);

        g.fill(2, 2, 6, 6, this.type.color);
    }

    @Override
    public boolean onMouseDown(double x, double y, EditorScreen.EditorState state) {
        var oldConnection = state.getCurrentGraph().connections.stream().filter(
                conn -> conn.keyTo().equals(this.key) && conn.to().equals(this.owner)
        ).findFirst();

        if (oldConnection.isPresent()) {
            var old = oldConnection.get();
            state.draggingFrom = state.graphNodeToRenderableMap.get(old.from()).ports.get(old.keyFrom()).port;
            state.getCurrentGraph().connections.remove(old);
            state.markDirty();
        } else {
            state.draggingFrom = this;
        }

        return true;
    }

    @Override
    public boolean onMouseUp(double x, double y, EditorScreen.EditorState state) {
        if (x >= 0 && x <= this.layoutCache.width() && y >= 0 && y <= this.layoutCache.height()) {
            if (state.draggingFrom != null && state.draggingFrom != this && state.draggingFrom.owner != this.owner) {
                var fromDescriptor = state.getCurrentGraph().nodeDescriptors.get(state.draggingFrom.owner);
                var toDescriptor = state.getCurrentGraph().nodeDescriptors.get(this.owner);

                var draggedFromOutput = fromDescriptor.isOutput(state.draggingFrom.key);
                var draggedToOutput = toDescriptor.isOutput(this.key);

                if (draggedToOutput == !draggedFromOutput) {
                    var source = draggedFromOutput ? state.draggingFrom : this;
                    var target = !draggedFromOutput ? state.draggingFrom : this;

                    state.getCurrentGraph().removeConnection(source.owner, target.owner, source.key, target.key);

                    state.getCurrentGraph().connections.add(new PatchGraph.Connection(source.owner, target.owner, source.key, target.key));
                    state.markDirty();

                    state.draggingFrom = null;
                    return true;
                }
            }
        }

        return false;
    }

}
