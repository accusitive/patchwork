package party.stoat.patchwork.client.screen.components;

import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3x2f;

import java.util.List;

public class BackgroundColorNode<T extends Renderable> extends Renderable {
    int color;
    T child;

    public BackgroundColorNode(int color, T child) {
        this.color = color;
        this.child = child;
    }

    @Override
    public void paint(GuiGraphics g, Layout l, Matrix3x2f mat) {
        super.paint(g, l, mat);

        g.fill(0, 0, l.width(), l.height(), this.color);
    }

    @Override
    protected Layout extractInnerLayout(int x, int y) {
        var l = child.extractLayout(0, 0);

        return new Layout(x, y, l.width(), l.height(), this, List.of(l), false);
    }
}
