package party.stoat.patchwork.client.screen.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import org.joml.Matrix3x2f;
import org.lwjgl.glfw.GLFW;
import party.stoat.patchwork.client.screen.EditorScreen;

import java.util.List;

public class TextInput extends Renderable {

    public Text text;
    public int width;
    public int height;

    public int backgroundColor = FastColor.ARGB32.color(255, 100, 100, 100);
    public int highlightedColor = FastColor.ARGB32.color(255, 150, 150, 150);

    public final EditBox editBox;

    public boolean highlight = false;

    public TextInput(String content, int width, int height) {
        this.editBox = new EditBox(EditorScreen.FONT, width, height, Component.empty());
        this.editBox.setValue(content);

        this.text = new Text(content, 0xffffffff);
        this.width = width;
        this.height = height;
    }

    @Override
    public void paint(GuiGraphics g, Layout l) {
        super.paint(g, l);

        g.fill(0, 0, l.width(), l.height(), this.highlight ? this.highlightedColor : this.backgroundColor);
//        this.editBox.setValue("yuuup");
        this.editBox.extractWidgetRenderState(g, 0, 0, 1.0f);
    }

    @Override
    public boolean onMouseDown(double x, double y, EditorScreen.EditorState state) {
        this.editBox.setFocused(true);

        return true;
    }

    @Override
    public void onMouseDownGlobal(double x, double y, EditorScreen.EditorState state) {
        if(this.layoutCache != null) {
            this.editBox.setFocused(this.layoutCache.contains(x, y));
        }
    }

    @Override
    public boolean charTyped(char c, EditorScreen.EditorState state) {
        // charTyped(char c, int keyCode)
        return this.editBox.charTyped(c, state);
    }

    @Override
    public void onKeyDown(int key, int scancode, int mods) {
//        if(this.highlight) this.editBox.keyPressed(event);
        if(key == GLFW.GLFW_KEY_ENTER) {
            this.editBox.setFocused(false);
        }

        if(this.editBox.isFocused()) {
            this.editBox.keyPressed(key, scancode, mods);
        }
    }

    @Override
    public void onKeyUp(int key, int scancode, int mods) {
        this.editBox.keyPressed(key, scancode, mods);
    }

    @Override
    protected Layout extractInnerLayout(int x, int y, int z) {
        this.editBox.setX(x);
        this.editBox.setY(y);

        return new Layout(x, y, z, this.width + x, this.height + y, this, List.of(), false);
    }
}
