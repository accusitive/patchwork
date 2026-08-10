package party.stoat.patchwork.client.screen.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FastColor;
import party.stoat.patchwork.client.screen.EditorScreen;

import java.util.List;

public class DirectionButton extends AbstractButton {

    NodeIOConfiguring parent;

    public int width;
    public int height;

    public int backgroundColor = FastColor.ARGB32.color(255, 100, 100, 100);
    public int highlightedColor = FastColor.ARGB32.color(255, 150, 150, 150);

    public boolean highlight = false;

    private ButtonHandler onClick;

    public DirectionButton(NodeIOConfiguring parent, int width, int height, ButtonHandler onClick) {
        super(width, height, onClick);

        this.width = width;
        this.height = height;
        this.parent = parent;
    }

    @Override
    public void paint(GuiGraphics g, Layout l) {
        super.paint(g, l);
        var direction = parent.direction;

        var offsetX = 7;
        var offsetY = 1;
        if (parent.direction == null) {
            g.drawString(EditorScreen.FONT, "-", offsetX+1, offsetY+1, 0xff000000);
            g.drawString(EditorScreen.FONT, "-", offsetX, offsetY, 0xffffffff);
        } else if (parent.parent.displayDirectionAsRelative) {
            String content = switch (parent.direction) {
                case DOWN -> "Bo";
                case UP -> "T";
                case NORTH -> "F";
                case SOUTH -> "Ba";
                case WEST -> "R";
                case EAST -> "L";
            };
            g.drawString(EditorScreen.FONT, content, offsetX+1, offsetY+1, 0xff000000);
            g.drawString(EditorScreen.FONT, content, offsetX, offsetY, 0xffffffff);
        } else {
            String content = switch (parent.direction) {
                case DOWN -> "D";
                case UP -> "U";
                case NORTH -> "N";
                case SOUTH -> "S";
                case WEST -> "W";
                case EAST -> "E";
            };
            g.drawString(EditorScreen.FONT, content, offsetX+1, offsetY+1, 0xff000000);
            g.drawString(EditorScreen.FONT, content, offsetX, offsetY, 0xffffffff);
        }
        g.fill(0, 0, l.width(), l.height(), this.highlight ? this.highlightedColor : this.backgroundColor);

    }

    @Override
    protected Layout extractInnerLayout(int x, int y, int z) {
        return new Layout(x, y, z, this.width, this.height, this, List.of(), false);
    }

}
