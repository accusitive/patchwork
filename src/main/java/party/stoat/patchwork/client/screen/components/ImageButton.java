package party.stoat.patchwork.client.screen.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3x2f;
import party.stoat.patchwork.Patchwork;
import party.stoat.patchwork.client.screen.EditorScreen;

import java.util.List;

public class ImageButton extends AbstractButton {

    public ResourceLocation image;
    public Text text;
    public int width;
    public int height;

    public static final ResourceLocation SCHEMATIC_BUTTON = ResourceLocation.fromNamespaceAndPath(Patchwork.MOD_ID, "textures/gui/schematic_button.png");
    public static final ResourceLocation SCHEMATIC_BUTTON_ACTIVE = ResourceLocation.fromNamespaceAndPath(Patchwork.MOD_ID, "textures/gui/schematic_button_active.png");
    public static final ResourceLocation SAVE = ResourceLocation.fromNamespaceAndPath(Patchwork.MOD_ID, "textures/gui/save.png");
    public static final ResourceLocation PLUS = ResourceLocation.fromNamespaceAndPath(Patchwork.MOD_ID, "textures/gui/plus.png");

    public int paddingX = 0;
    public int paddingY = 0;

    public boolean highlight = false;

    public ImageButton(ResourceLocation image, int width, int height, ButtonHandler onClick) {
        super(width, height, onClick);

        this.image = image;
        this.width = width;
        this.height = height;
    }

    @Override
    public void paint(GuiGraphics g, Layout l) {
        super.paint(g, l);

        g.blit(this.image, this.paddingX, this.paddingY, this.width + this.paddingX, this.height + this.paddingY, 0, 1, 0, 1);

        if(this.highlight) {
            g.fill(this.paddingX + 1, this.paddingY + 1, this.width + this.paddingX - 1, this.height + this.paddingY - 1, 0x22ffffff);
        }
    }

    @Override
    public void onMouseMove(double x, double y, EditorScreen.EditorState state) {
        if(this.layoutCache == null) return;
        this.highlight = x >= 0 && x <= this.layoutCache.width() && y >= 0 && y <= this.layoutCache.height();
    }

    @Override
    protected Layout extractInnerLayout(int x, int y, int z) {
        if(this.text != null) {
            this.text.extractLayout(0, 0, 0);
            var textLayout = this.text.extractLayout((this.width - text.layoutCache.width()) / 2, (this.height - text.layoutCache.height()) / 2, 0);
            return new Layout(x, y, z, this.width + (this.paddingX * 2), this.height + (this.paddingY * 2), this, List.of(textLayout), false);
        }

        return new Layout(x, y, z, this.width + (this.paddingX * 2), this.height + (this.paddingY * 2), this, List.of(), false);
    }
}
