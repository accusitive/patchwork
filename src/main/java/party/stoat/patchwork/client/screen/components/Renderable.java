package party.stoat.patchwork.client.screen.components;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import party.stoat.patchwork.client.screen.EditorScreen;

import java.util.List;

public abstract class Renderable {
    public int offsetX;
    public int offsetY;

    public Layout layoutCache;

    public int absX;
    public int absY;

    public record Layout(int x, int y, int width, int height, Renderable r, List<Layout> children, boolean disabled, boolean scissor, float scale) {

        public Layout(int x, int y, int width, int height, Renderable r, List<Layout> children, boolean disabled, boolean scissor) {
            this(x, y, width, height, r, children, disabled, scissor, 1.0f);
        }

        public Layout(int x, int y, int width, int height, Renderable r, List<Layout> children, boolean disabled) {
            this(x, y, width, height, r, children, disabled, true, 1.0f);
        }

        public void paint(GuiGraphicsExtractor g) {
//            g.pose().translate(this.x() / this.scale, this.y() / this.scale);
            g.pose().pushMatrix();
            g.pose().scale(this.scale());
            var mat = g.pose().translate(this.x, this.y);
            if(this.scissor) g.enableScissor(0, 0, this.width, this.height);
            this.r.paint(g, this, mat);
            this.children.forEach(c -> c.paint(g));
            if(this.scissor) g.disableScissor();
            g.pose().popMatrix();
//            g.pose().popMatrix();
        }

        public void onKeyDown(KeyEvent event) {
            this.children.forEach(c -> c.onKeyDown(event));
            this.r.onKeyDown(event);
        }

        public boolean charTyped(CharacterEvent event, EditorScreen.EditorState state) {
            this.children.forEach(c -> c.charTyped(event, state));
            this.r.charTyped(event, state);

            return true;
        }

        public void onKeyUp(KeyEvent event) {
            this.children.forEach(c -> c.onKeyUp(event));
            this.r.onKeyUp(event);
        }

        public boolean contains(double x, double y) {
            return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height;
        }

        public void onMouseDownGlobal(double x, double y, EditorScreen.EditorState state) {
            this.children.forEach(c -> c.onMouseDownGlobal(x / this.scale - this.x, y / this.scale - this.y, state));

            this.r.onMouseDownGlobal(x / this.scale - this.x, y / this.scale - this.y, state);
        }

        public boolean onMouseDown(double x, double y, EditorScreen.EditorState state) {
            if(this.disabled) return false;

            if (this.contains(x / this.scale, y / this.scale) || !this.scissor) {
                for (var c : this.children) {
                    if (c.onMouseDown((x / this.scale) - this.x, (y / this.scale) - this.y, state)) return true;
                }

                return this.r.onMouseDown((x / this.scale) - this.x, (y / this.scale) - this.y, state);
            }

            return false;
        }

        public boolean onMouseUp(double x, double y, EditorScreen.EditorState state) {
            if(this.disabled) return false;

            for (var c : this.children) {
                if (c.onMouseUp((x / this.scale) - this.x, (y / this.scale) - this.y, state)) return true;
            }

            return this.r.onMouseUp((x / this.scale) - this.x, (y / this.scale) - this.y, state);
        }

        public void onMouseMove(double x, double y, EditorScreen.EditorState state) {
            if(this.disabled) return;

            for (var c : this.children) {
                c.onMouseMove((x / this.scale) - this.x, (y / this.scale) - this.y, state);
            }

            this.r.onMouseMove((x / this.scale) - this.x, (y / this.scale) - this.y, state);
        }

        public void onScroll(double x, double y, double scrollX, double scrollY) {
            if(this.disabled) return;

            if (this.contains(x / this.scale, y / this.scale) || !this.scissor) {
                for (var c : this.children) {
                    c.onScroll((x / this.scale) - this.x, (y / this.scale) - this.y, scrollX, scrollY);
                }

                this.r.onScroll((x / this.scale) - this.x, (y / this.scale) - this.y, scrollX, scrollY);
            }
        }
    }

    public boolean charTyped(CharacterEvent event, EditorScreen.EditorState state) {
        return false;
    }

    public boolean onMouseDown(double x, double y, EditorScreen.EditorState state) {
        return false;
    }

    public void onMouseDownGlobal(double x, double y, EditorScreen.EditorState state) {}

    public void onKeyDown(KeyEvent event) {}

    public void onKeyUp(KeyEvent event) {}

    public void onScroll(double x, double y, double scrollX, double scrollY) {}

    public void onMouseMove(double x, double y, EditorScreen.EditorState state) {}

    public boolean onMouseUp(double x, double y, EditorScreen.EditorState state) {
        return false;
    }

    public void paint(GuiGraphicsExtractor g, Layout l, Matrix3x2f mat) {
        var point = mat.transformPosition(new Vector2f(this.offsetX, this.offsetY));
        this.absX = (int) point.x;
        this.absY = (int) point.y;
    }

    public Layout extractLayout(int x, int y) {
        this.layoutCache = this.extractInnerLayout(x + this.offsetX, y + this.offsetY);
        return layoutCache;
    }

    protected abstract Layout extractInnerLayout(int x, int y);

}
