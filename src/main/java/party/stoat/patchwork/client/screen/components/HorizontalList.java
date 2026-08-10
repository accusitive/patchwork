package party.stoat.patchwork.client.screen.components;

import java.util.ArrayList;
import java.util.List;

public class HorizontalList<T extends Renderable> extends Renderable {

    public ArrayList<T> elements;
    public int padding;

    public int width = 0;
    public int height = 0;

    public HorizontalList(ArrayList<T> elements, int padding) {
        this.elements = elements;
        this.padding = padding;
    }

    @Override
    protected Layout extractInnerLayout(int dX, int dY, int dZ) {

        int maxHeight = this.height;
        int listX = 0;

        var c = new ArrayList<Layout>();

        for (var e : this.elements) {
            var childLayout = e.extractLayout(listX, 0, 0);

            listX += childLayout.width() + this.padding;
            maxHeight = Math.max(maxHeight, childLayout.height());

            c.add(childLayout);
        }

        return new Layout(
                dX,
                dY,
                dZ,
                Math.max(listX - padding, this.width),
                maxHeight,
                this,
                c,
                false
        );
    }
}