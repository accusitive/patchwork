package party.stoat.patchwork.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class BezierCurveRenderer {

    private BezierCurveRenderer() {}

    public static void render(GuiGraphics graphics, List<EditorScreen.Line> lines) {
        PoseStack poseStack = graphics.pose();

        MultiBufferSource.BufferSource buffer =
                Minecraft.getInstance().renderBuffers().bufferSource();

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.gui());

        var pose = poseStack.last().pose();

        for (var points : lines) {
            drawRibbon(vertexConsumer, pose, points, 1.4f,
                    (points.color() & 0x00FFFFFF) | 0xCC000000);

            drawRibbon(vertexConsumer, pose, points, 0.7f,
                    points.color());
        }

        buffer.endBatch(RenderType.gui());
    }

    private static void drawRibbon(
            VertexConsumer vertexConsumer,
            org.joml.Matrix4f pose,
            EditorScreen.Line points,
            float halfWidth,
            int color
    ) {
        for (int i = 0; i < points.points().size() - 1; i++) {

            Vec3 a3 = points.points().get(i);
            Vec3 b3 = points.points().get(i + 1);

            Vec2 a = new Vec2((float) a3.x, (float) a3.y);
            Vec2 b = new Vec2((float) b3.x, (float) b3.y);

            Vec2 dir = b.add(a.negated()).normalized();

            Vec2 ortho1 = new Vec2(-dir.y, dir.x).scale(halfWidth);
            Vec2 ortho2 = new Vec2(dir.y, -dir.x).scale(halfWidth);

            float x1 = ortho1.add(a).x;
            float x2 = ortho2.add(a).x;
            float x3 = ortho1.add(b).x;
            float x4 = ortho2.add(b).x;

            float y1 = ortho1.add(a).y;
            float y2 = ortho2.add(a).y;
            float y3 = ortho1.add(b).y;
            float y4 = ortho2.add(b).y;

            vertexConsumer.addVertex(pose, x1, y1, 0).setColor(color);
            vertexConsumer.addVertex(pose, x2, y2, 0).setColor(color);
            vertexConsumer.addVertex(pose, x3, y3, 0).setColor(color);

            vertexConsumer.addVertex(pose, x2, y2, 0).setColor(color);
            vertexConsumer.addVertex(pose, x4, y4, 0).setColor(color);
            vertexConsumer.addVertex(pose, x3, y3, 0).setColor(color);
        }
    }
}