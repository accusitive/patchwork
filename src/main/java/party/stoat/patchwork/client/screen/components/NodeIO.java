package party.stoat.patchwork.client.screen.components;

import party.stoat.patchwork.client.screen.EditorScreen;
import party.stoat.patchwork.patchgraph.NodeDescriptor;

import java.util.List;
import java.util.UUID;

public class NodeIO extends Renderable {

    Text display;
    public NodeIOPort port;

    boolean rightAlign = false;
    boolean disabled = false;

    public NodeDescriptor.DataType type;

    public NodeIO(String display, String key, UUID owner, boolean rightAlign, boolean disabled, NodeDescriptor.DataType type) {
        this.port = new NodeIOPort(key, owner, type);
        this.display = new Text(display, 0xffffffff);
        this.rightAlign = rightAlign;
        this.disabled = disabled;
    }

    @Override
    protected Layout extractInnerLayout(int x, int y, int z) {
        Layout displayLayout;
        Layout portLayout;

        int w = EditorScreen.FONT.width(this.display.content) + 4;

        if (this.rightAlign) {
            displayLayout = display.extractLayout(0, 0, 0);
            portLayout = port.extractLayout(w, 2, 0);
        } else {
            displayLayout = display.extractLayout(8, 0, 0);
            portLayout = port.extractLayout(0, 2, 0);
        }

        return new Layout(x, y, z, w + 4, EditorScreen.FONT.lineHeight, this, List.of(displayLayout, portLayout), this.disabled);
    }

}
