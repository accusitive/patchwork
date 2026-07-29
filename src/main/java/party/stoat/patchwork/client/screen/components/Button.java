package party.stoat.patchwork.client.screen.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FastColor;
import org.joml.Matrix3x2f;
import party.stoat.patchwork.client.screen.EditorScreen;

import java.util.List;

public class Button extends AbstractButton {

    public Text text;
    public int width;
    public int height;

    private final ButtonHandler onClick;

    public int backgroundColor = FastColor.ARGB32.color(255, 100, 100, 100);
    public int highlightedColor = FastColor.ARGB32.color(255, 150, 150, 150);

    public boolean highlight = false;

    public Button(String content, int width, int height, ButtonHandler onClick) {
        super(width, height, onClick);

        this.text = new Text(content, 0xffffffff);
        this.width = width;
        this.height = height;
        this.onClick = onClick;
    }

    @Override
    public void paint(GuiGraphics g, Layout l) {
        super.paint(g, l);

        g.fill(0, 0, l.width(), l.height(), this.highlight ? this.highlightedColor : this.backgroundColor);
    }

    @Override
    protected Layout extractInnerLayout(int x, int y, int z) {
        this.text.extractLayout(0, 0, 0);
        var textLayout = this.text.extractLayout((this.width - text.layoutCache.width()) / 2, (this.height - text.layoutCache.height()) / 2, 0);

        return new Layout(x, y, z, this.width, this.height, this, List.of(textLayout), false);
    }
}
