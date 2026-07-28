package party.stoat.patchwork.client.screen.components;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2f;
import party.stoat.patchwork.client.screen.EditorScreen;

public class CanvasNode extends ScissorNode {
    public CanvasNode(Renderable child, boolean draggable, boolean zoomable) {
        super(child, draggable, zoomable);
    }

    @Override
    public void paint(GuiGraphicsExtractor g, Layout l, Matrix3x2f mat) {
        super.paint(g, l, mat);

        int gridSize = 15;

        if(this.layoutCache != null) {
            var cache = this.layoutCache;

            for(int x=0;x<(cache.width() / gridSize) + 1;x++) {
                int renderX = (int) ((x * gridSize / this.scale) + (this.innerOffsetX / this.scale % gridSize));
                g.verticalLine(renderX, 0, cache.height(), 0x11555555);
            }

            for(int y=0;y<(cache.height() / gridSize) + 1;y++) {
                int renderY = (int) ((y * gridSize / this.scale) + (this.innerOffsetY / this.scale % gridSize));
                g.horizontalLine(cache.x(), cache.x() + cache.width(), renderY, 0x11555555);
            }
        }
    }

    @Override
    public boolean onMouseDown(double x, double y, EditorScreen.EditorState state) {
        if(state.nodeBeingEdited != null) {
            state.nodeBeingEdited.setConfiguring(false, state);
        }

        return super.onMouseDown(x, y, state);
    }
}
