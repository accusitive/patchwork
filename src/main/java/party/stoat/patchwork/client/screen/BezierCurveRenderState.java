package party.stoat.patchwork.client.screen;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import party.stoat.patchwork.PatchworkClient;

import java.util.List;


public class BezierCurveRenderState implements GuiElementRenderState {
    List<EditorScreen.Line> lines;
    public BezierCurveRenderState(List<EditorScreen.Line> lines) {
        this.lines = lines;
    }
    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        for(var points : lines) {
            float hwidth = 0.7f;

            for (int i = 0; i < points.points().size() - 1; i++) {
                Vec3 a3 = points.points().get(i);
                Vec3 b3 = points.points().get(i + 1);

                Vec2 a = new Vec2((float) a3.x, (float) a3.y);
                Vec2 b = new Vec2((float) b3.x, (float) b3.y);

                var dir = b.add(a.negated()).normalized();

                var ortho1 = new Vec2(-dir.y, dir.x).scale(hwidth * 2.0f);
                var ortho2 = new Vec2(dir.y, -dir.x).scale(hwidth * 2.0f);

                var x1 = ortho1.add(a).x;
                var x2 = ortho2.add(a).x;
                var x3 = ortho1.add(b).x;
                var x4 = ortho2.add(b).x;

                var y1 = ortho1.add(a).y;
                var y2 = ortho2.add(a).y;
                var y3 = ortho1.add(b).y;
                var y4 = ortho2.add(b).y;

                int col = (points.color() & 0x00ffffff) | 0xcc000000;

                vertexConsumer.addVertex(x1, y1, 0.0f).setColor(col).setLineWidth((float) a3.z);
                vertexConsumer.addVertex(x2, y2, 0.0f).setColor(col).setLineWidth((float) a3.z);
                vertexConsumer.addVertex(x3, y3, 0.0f).setColor(col).setLineWidth((float) a3.z);
                vertexConsumer.addVertex(x2, y2, 0.0f).setColor(col).setLineWidth((float) b3.z);
                vertexConsumer.addVertex(x4, y4, 0.0f).setColor(col).setLineWidth((float) b3.z);
                vertexConsumer.addVertex(x3, y3, 0.0f).setColor(col).setLineWidth((float) b3.z);
            }

            for (int i = 0; i < points.points().size() - 1; i++) {
                Vec3 a3 = points.points().get(i);
                Vec3 b3 = points.points().get(i + 1);

                Vec2 a = new Vec2((float) a3.x, (float) a3.y);
                Vec2 b = new Vec2((float) b3.x, (float) b3.y);

                var dir = b.add(a.negated()).normalized();

                var ortho1 = new Vec2(-dir.y, dir.x).scale(hwidth);
                var ortho2 = new Vec2(dir.y, -dir.x).scale(hwidth);

                var x1 = ortho1.add(a).x;
                var x2 = ortho2.add(a).x;
                var x3 = ortho1.add(b).x;
                var x4 = ortho2.add(b).x;

                var y1 = ortho1.add(a).y;
                var y2 = ortho2.add(a).y;
                var y3 = ortho1.add(b).y;
                var y4 = ortho2.add(b).y;

                vertexConsumer.addVertex(x1, y1, 0.0f).setColor(points.color()).setLineWidth((float) a3.z);
                vertexConsumer.addVertex(x2, y2, 0.0f).setColor(points.color()).setLineWidth((float) a3.z);
                vertexConsumer.addVertex(x3, y3, 0.0f).setColor(points.color()).setLineWidth((float) a3.z);
                vertexConsumer.addVertex(x2, y2, 0.0f).setColor(points.color()).setLineWidth((float) b3.z);
                vertexConsumer.addVertex(x4, y4, 0.0f).setColor(points.color()).setLineWidth((float) b3.z);
                vertexConsumer.addVertex(x3, y3, 0.0f).setColor(points.color()).setLineWidth((float) b3.z);
            }

        }
    }

    @Override
    public RenderPipeline pipeline() {
        return PatchworkClient.LINE;
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        // TODO: iterate points and find bounds? Other GuiElementRenderState implementers do this
        return null;
    }


    @Override
    public @Nullable ScreenRectangle bounds() {
        return new ScreenRectangle(0, 0, 1920, 1080);
    }
}
