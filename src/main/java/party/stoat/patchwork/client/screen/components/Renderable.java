package party.stoat.patchwork.client.screen.components;

import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import party.stoat.patchwork.client.screen.EditorScreen;

import java.util.List;

public abstract class Renderable {
    public int offsetX;
    public int offsetY;

    public Layout layoutCache;

    public int absX;
    public int absY;
    public int absZ;

    public record Layout(int x, int y, int z, int width, int height, Renderable r, List<Layout> children, boolean disabled, boolean scissor, float scale) {

        public Layout(int x, int y, int z, int width, int height, Renderable r, List<Layout> children, boolean disabled, boolean scissor) {
            this(x, y, z, width, height, r, children, disabled, scissor, 1.0f);
        }

        public Layout(int x, int y, int z, int width, int height, Renderable r, List<Layout> children, boolean disabled) {
            this(x, y, z, width, height, r, children, disabled, true, 1.0f);
        }

        public void paint(GuiGraphics g) {
//            g.pose().translate(this.x() / this.scale, this.y() / this.scale);
            g.pose().pushPose();
            g.pose().scale(scale, scale, scale);
            g.pose().translate(this.x, this.y, this.z);
//            if(this.scissor) g.enableScissor(this.x, this.y, this.x + this.width, this.y + this.height);
            this.r.paint(g, this);
            this.children.forEach(c -> c.paint(g));
//            if(this.scissor) g.disableScissor();
            g.pose().popPose();
//            g.pose().popMatrix();
        }

        public void onKeyDown(int key, int scancode, int mods) {
            this.children.forEach(c -> c.onKeyDown(key, scancode, mods));
            this.r.onKeyDown(key, scancode, mods);
        }

        public boolean charTyped(char c, int keyCode, EditorScreen.EditorState state) {
            this.children.forEach(character -> character.charTyped(c, keyCode, state));
            this.r.charTyped(c, keyCode, state);

            return true;
        }

        public void onKeyUp(int key, int scancode, int mods) {
            this.children.forEach(c -> c.onKeyUp(key, scancode, mods));
            this.r.onKeyUp(key, scancode, mods);
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

    public boolean charTyped(char c, int keyCode, EditorScreen.EditorState state) {
        return false;
    }

    public boolean onMouseDown(double x, double y, EditorScreen.EditorState state) {
        return false;
    }

    public void onMouseDownGlobal(double x, double y, EditorScreen.EditorState state) {}

    public void onKeyDown(int key, int scancode, int mods) {}

    public void onKeyUp(int key, int scancode, int mods) {}

    public void onScroll(double x, double y, double scrollX, double scrollY) {}

    public void onMouseMove(double x, double y, EditorScreen.EditorState state) {}

    public boolean onMouseUp(double x, double y, EditorScreen.EditorState state) {
        return false;
    }

    public void paint(GuiGraphics g, Layout l) {
        Matrix4f matrix = g.pose().last().pose();

        Vector4f point = new Vector4f(offsetX, offsetY, 0.0f, 1.0f);
        point.mul(matrix);

        absX = (int) point.x();
        absY = (int) point.y();
    }

    public Layout extractLayout(int x, int y, int z) {
        this.layoutCache = this.extractInnerLayout(x + this.offsetX, y + this.offsetY, z);
        return layoutCache;
    }

    protected abstract Layout extractInnerLayout(int x, int y, int z);

}
