package party.stoat.patchwork.client.screen.components;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2f;
import party.stoat.patchwork.client.screen.EditorScreen;

import java.util.List;

public class    Text extends Renderable {

    String content;
    int color;

    public Text(String content, int color) {
        this.content = content;
        this.color = color;
    }

    @Override
    protected Layout extractInnerLayout(int x, int y) {
        return new Layout(x, y, EditorScreen.FONT.width(this.content), EditorScreen.FONT.lineHeight, this, List.of(), false);
    }

    @Override
    public void paint(GuiGraphicsExtractor g, Layout l, Matrix3x2f mat) {
        super.paint(g, l, mat);
        
        g.text(EditorScreen.FONT, this.content,0, 0, this.color);
    }
}
