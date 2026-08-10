package party.stoat.patchwork.client.screen.components;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import org.lwjgl.glfw.GLFW;
import party.stoat.patchwork.client.screen.EditorScreen;
import party.stoat.patchwork.patchgraph.NodeDescriptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class NodeIOConfiguring extends Renderable {

    TextInput display;
    public NodeIOPort port;

    public RenderableGraphNode parent;
    boolean rightAlign = false;
    boolean disabled = false;

    public NodeDescriptor.DataType type;

    public Button typeSelector;
    public DirectionButton directionSelector;
    public Button remove;

    public int directionOrdinal;
    public Direction direction;

    public NodeIOConfiguring(String display, String key, UUID owner, RenderableGraphNode parent, boolean rightAlign, boolean disabled, NodeDescriptor.DataType type, Optional<Direction> direction, AbstractButton.ButtonHandler handler) {
        this.port = new NodeIOPort(key, owner, type);
        this.display = new TextInput(display, 80, EditorScreen.FONT.lineHeight + 1);
        this.parent = parent;
        this.rightAlign = rightAlign;
        this.disabled = disabled;
        this.type = type;
        this.direction = direction.orElse(null);
        this.directionOrdinal = direction.map(Direction::ordinal).orElse(6);

        this.typeSelector = new Button(this.type.name().substring(0, 1), 30, 10, (btn, state) -> {
            this.type = NodeDescriptor.DataType.values()[(this.type.ordinal() + 1) % NodeDescriptor.DataType.values().length];
            this.typeSelector.text.content = this.type.name().substring(0, 1);
        });

        this.remove = new Button("-", 30, 10, handler);
        this.remove.backgroundColor = 0xffff0000;

        this.directionSelector = new DirectionButton(this, 20, 10, (btn, state) -> {
            if (GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) {
                this.directionOrdinal--;
                if (this.directionOrdinal < 0) {
                    this.directionOrdinal = 6;
                }
            } else {
                this.directionOrdinal++;
                if (this.directionOrdinal > Direction.values().length + 1) {
                    this.directionOrdinal = 0;
                }

            }

            this.direction = this.directionOrdinal < 6 ? Direction.values()[this.directionOrdinal] : null;
        });
    }

    @Override
    protected Layout extractInnerLayout(int x, int y, int z) {
        Layout displayLayout;
        Layout dirLayout;
        Layout removeLayout;

        var l = this.typeSelector.extractLayout(0, 0, 0);
        dirLayout = this.directionSelector.extractLayout(l.width() + 1, 0, 0);
        removeLayout = this.remove.extractLayout(dirLayout.x() + dirLayout.width() + 1, 0, 0);
        displayLayout = display.extractLayout(44, 0, 0);

        var children = List.of(removeLayout, displayLayout, l, dirLayout);
//        var children = List.of(displayLayout);


        return new Layout(x, y, z, 180, EditorScreen.FONT.lineHeight, this, children, this.disabled);
    }

}
