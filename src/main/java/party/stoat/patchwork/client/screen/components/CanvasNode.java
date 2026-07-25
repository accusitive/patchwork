package party.stoat.patchwork.client.screen.components;

import party.stoat.patchwork.client.screen.EditorScreen;

public class CanvasNode extends ScissorNode {
    public CanvasNode(Renderable child, boolean draggable, boolean zoomable) {
        super(child, draggable, zoomable);
    }

    @Override
    public boolean onMouseDown(double x, double y, EditorScreen.EditorState state) {
        if(state.nodeBeingEdited != null) {
            state.nodeBeingEdited.setConfiguring(false, state);
        }

        return super.onMouseDown(x, y, state);
    }
}
