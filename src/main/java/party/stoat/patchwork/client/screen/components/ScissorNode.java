package party.stoat.patchwork.client.screen.components;

import party.stoat.patchwork.client.screen.EditorScreen;

import java.util.List;

public class ScissorNode extends Renderable {

    public int innerOffsetX = 0;
    public int innerOffsetY = 0;

    public int width = 0;
    public int height = 0;

    public boolean draggable;
    public boolean zoomable;

    private int mX = -1;
    private int mY = -1;
    private boolean dragging = false;
    public double scale = 1.0;

    public Renderable child;

    public ScissorNode(Renderable child, boolean draggable, boolean zoomable) {
        this.child = child;
        this.draggable = draggable;
        this.zoomable = zoomable;
    }

    @Override
    public boolean onMouseDown(double x, double y, EditorScreen.EditorState state) {
        dragging = this.draggable;

        mX = (int) (x - this.innerOffsetX);
        mY = (int) (y - this.innerOffsetY);

        return false;
    }

    @Override
    public void onScroll(double x, double y, double scrollX, double scrollY) {
        if(this.zoomable) {
            var newScale = this.scale + (scrollY * 0.05);
            newScale = Math.clamp(newScale, 0.1, 2.0);
            if(newScale == this.scale) return;
            this.innerOffsetX += (int) (x * (scale - newScale));
            this.innerOffsetY += (int) (y * (scale - newScale));
            this.scale = newScale;
        }
    }

    @Override
    public void onMouseMove(double x, double y, EditorScreen.EditorState state) {
        if(this.dragging) {
            this.innerOffsetX = (int) (x - mX);
            this.innerOffsetY = (int) (y - mY);
        }
    }

    @Override
    public boolean onMouseUp(double x, double y, EditorScreen.EditorState state) {
        this.dragging = false;
        return false;
    }

    @Override
    protected Layout extractInnerLayout(int x, int y, int z) {
        var childLayout = child.extractLayout(innerOffsetX, innerOffsetY, 0);

        return new Layout((int) (x / this.scale), (int) (y / this.scale), (int) ((width != 0 ? width : childLayout.width()) / this.scale), (int) ((height != 0 ? height : childLayout.height()) / this.scale), this, List.of(childLayout), false, true, (float) this.scale);
    }
}
