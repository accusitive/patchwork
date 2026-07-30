package party.stoat.patchwork.client.screen;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.joml.Matrix3x2f;
import org.lwjgl.glfw.GLFW;
import party.stoat.patchwork.Patchwork;
import party.stoat.patchwork.patchgraph.StorageConfiguration;
import party.stoat.patchwork.block.sf_controller.SFControllerMenu;
import party.stoat.patchwork.client.Bezier4;
import party.stoat.patchwork.client.screen.components.*;
import party.stoat.patchwork.patchgraph.NodeDescriptor;
import party.stoat.patchwork.patchgraph.PatchGraph;
import party.stoat.patchwork.patchgraph.nodes.SFSystemPowerNode;
import party.stoat.patchwork.network.CreatePatchServerboundPayload;
import party.stoat.patchwork.network.UpdatePatchServerboundPayload;
import party.stoat.patchwork.patchgraph.nodes.SplitterNode;

import javax.annotation.Nullable;
import java.util.*;

public class EditorScreen extends AbstractContainerScreen<SFControllerMenu> implements GuiEventListener {

    public static Font FONT = Minecraft.getInstance().font;

    private static final ResourceLocation CONTAINER_TEXTURE = ResourceLocation.fromNamespaceAndPath(Patchwork.MOD_ID, "textures/gui/container/inventory.png");
    public static final ResourceLocation MAGNIFYING_GLASS_TEXTURE = ResourceLocation.fromNamespaceAndPath(Patchwork.MOD_ID, "textures/gui/magnifying_glass.png");
    public static final ResourceLocation EJECT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Patchwork.MOD_ID, "textures/gui/eject.png");
    public static final ResourceLocation WRENCH_TEXTURE = ResourceLocation.fromNamespaceAndPath(Patchwork.MOD_ID, "textures/gui/eject.png");

    private static final int CONTAINER_WIDTH = 175;
    private static final int CONTAINER_HEIGHT = 90;

    private Renderable.Layout lastLayout;
    private Renderable root;
    public EditorState state;

    private Renderable rightSidebar;
    private Renderable leftSidebar;
    private ScissorNode canvas;
    private Renderable saveButton = new ImageButton(ImageButton.SAVE, 28, 28, (btn, state) -> this.save());

    public EditorScreen(SFControllerMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        this.width = CONTAINER_WIDTH;
        this.height = CONTAINER_HEIGHT;
        this.state = new EditorState();
        this.state.menu = menu;

        var window = Minecraft.getInstance().getWindow();
    }

    @Override
    protected void init() {
        super.init();

        this.topPos = this.height - this.imageHeight;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        // TODO: implement
    }

    public static class EditorState {

        @Nullable
        public NodeIOPort draggingFrom;
        public SFControllerMenu menu;

        public BlockPos controllerPos;

        public int currentGraph;

        public boolean editorDirty = false;

        public boolean shiftPressed = false;
        public Many graphNodes = new Many(new ArrayList<>());
        public HashMap<UUID, RenderableGraphNode> graphNodeToRenderableMap = new HashMap<>();

        public ArrayList<PatchGraph> patchGraphs = new ArrayList<>();
        public ArrayList<RenderableGraphNode> selectedNodes = new ArrayList<>();

        public ArrayList<StorageConfiguration.NodeCategory> serverProvidedDescriptors = new ArrayList<>();

        public @Nullable RenderableGraphNode nodeBeingEdited;

        public void markDirty() {
            this.editorDirty = true;
        }

        public @Nullable PatchGraph getCurrentGraph() {
            if(this.currentGraph >= this.patchGraphs.size()) this.currentGraph = -1;
            return this.currentGraph != -1 ? this.patchGraphs.get(this.currentGraph) : null;
        }

    }

    abstract static class NodeIcon extends Renderable {
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        if(this.lastLayout != null) {
            this.lastLayout.charTyped(c, keyCode, state);
        }

        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.lastLayout != null) {
            var result = this.lastLayout.onMouseDown((int) mouseX, (int) mouseY, this.state);
            this.lastLayout.onMouseDownGlobal((int) mouseX, (int) mouseY, this.state);

            if(!result) {
                state.selectedNodes.forEach(node -> node.highlighted = false);
                state.selectedNodes.clear();
            }
        }

        super.mouseClicked(mouseX, mouseY, button);

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.lastLayout != null) {
            this.lastLayout.onMouseUp((int) mouseX, (int) mouseY, this.state);
        }

        super.mouseReleased(mouseX, mouseY, button);

        state.draggingFrom = null;

        return true;
    }

    @Override
    public void mouseMoved(double x, double y) {
        if (this.lastLayout != null) {
            this.lastLayout.onMouseMove((int) x, (int) y, this.state);
        }

        super.mouseMoved(x, y);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if(this.lastLayout != null) this.lastLayout.onScroll(x, y, scrollX, scrollY);

        super.mouseScrolled(x, y, scrollX, scrollY);
        return true;
    }

    public void save() {
        if(state.getCurrentGraph() == null) return;

        if(state.nodeBeingEdited != null) {
            state.nodeBeingEdited.setConfiguring(false, this.state);
        }

        this.state.editorDirty = false;
        PacketDistributor.sendToServer(new UpdatePatchServerboundPayload(
                state.getCurrentGraph().graphId,
                state.getCurrentGraph(),
                this.state.controllerPos
        ));
    }

    @Override
    public boolean keyReleased(int key, int scancode, int mods) {
        if(key == GLFW.GLFW_KEY_LEFT_SHIFT) state.shiftPressed = false;

        if(this.lastLayout != null) this.lastLayout.onKeyUp(key, scancode, mods);

        return super.keyReleased(key, scancode, mods);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        if(Screen.hasControlDown() && key == GLFW.GLFW_KEY_S) {
            this.save();
            return true;
        }

        if(key == GLFW.GLFW_KEY_LEFT_SHIFT) state.shiftPressed = true;
        
        if(key == GLFW.GLFW_KEY_DELETE) {
            for(var node : state.selectedNodes) {
                if(state.getCurrentGraph() == null) break;
                state.getCurrentGraph().connections.removeIf(
                        conn -> conn.from().equals(node.uuid) || conn.to().equals(node.uuid)
                );
                this.state.graphNodes.elements.removeIf(renderable -> renderable.equals(node));
                state.getCurrentGraph().nodeDescriptors.remove(node.uuid);
            }
            state.selectedNodes.clear();
        }

        if(this.lastLayout != null) this.lastLayout.onKeyDown(key, scancode, mods);

        if(key == GLFW.GLFW_KEY_ESCAPE) {
            super.keyPressed(key, scancode, mods);
        }

        return true;
    }

    static class ItemStackNodeIcon extends NodeIcon {

        ItemStack stack;

        public ItemStackNodeIcon(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public void paint(GuiGraphics g, Layout l) {
            super.paint(g, l);
            g.renderItem(stack, 0, 0);
        }

        @Override
        protected Layout extractInnerLayout(int x, int y, int z) {
            return new Layout(x, y, z, 16, 16, this, List.of(), false);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void refresh(int width, int height) {
        this.leftSidebar = this.buildLeftSidebar();
        this.rightSidebar = this.buildRightSidebar();

        this.rightSidebar.offsetX = minecraft.getWindow().getGuiScaledWidth() - 200;

        if(this.canvas == null || this.rightSidebar == null) this.resize(minecraft, width, height);

        this.root = new Many(List.of(
            this.canvas,
            this.leftSidebar,
            this.rightSidebar
        ));
    }

    public Renderable buildRightSidebar() {
        List<Renderable> listElements = new ArrayList<>();
        listElements.add(new Text("Nodes", 0xffffffff));

        this.state.serverProvidedDescriptors.forEach(
                category -> listElements.add(new Dropdown(category.name(), category.nodes().stream().map(
                        descriptor ->
                                (Renderable) new RenderableGraphNode(
                                        descriptor,
                                        UUID.randomUUID(),
                                        true
                                )
                ).toList()) )
        );

        var builtin = new Dropdown("System", List.of(
                new RenderableGraphNode(
                        new NodeDescriptor(
                                "System Power",
                                List.of(),
                                List.of(
                                        new NodeDescriptor.IO("Power out", "out", new NodeDescriptor.Data(NodeDescriptor.DataType.Energy, false), Optional.empty())
                                ),
                                NodeDescriptor.DataType.Energy.color,
                                SFSystemPowerNode.IDENTIFIER,
                                ""
                        ),
                        UUID.randomUUID(),
                        true
                ),
                new RenderableGraphNode(
                        new NodeDescriptor(
                                "Splitter",
                                List.of(
                                        new NodeDescriptor.IO("Items in", "in", new NodeDescriptor.Data(NodeDescriptor.DataType.Item, false), Optional.empty())
                                ),
                                List.of(
                                        new NodeDescriptor.IO("Items out", "out", new NodeDescriptor.Data(NodeDescriptor.DataType.Item, true), Optional.empty())
                                ),
                                NodeDescriptor.DataType.Item.color,
                                SplitterNode.IDENTIFIER,
                                ""
                        ),
                        UUID.randomUUID(),
                        true
                )
        ));

        listElements.add(builtin);

        var list = new VerticalList<>(listElements, 4, false, false);
        list.width = 200;

        var scrollable = new Scrollable<>(list, list.width, minecraft.getWindow().getGuiScaledHeight());

        scrollable.offsetX = minecraft.getWindow().getGuiScaledWidth() - list.width;
        scrollable.offsetY = 5;

        return new BackgroundColorNode<>(FastColor.ARGB32.color(200, 25, 25, 35), scrollable);
    }

    @Override
    public void resize(Minecraft client, int width, int height) {
        super.resize(client, width, height);

        state.graphNodes.scissor = false;

        this.canvas = new CanvasNode(state.graphNodes, true, true);

        this.canvas.width = minecraft.getWindow().getGuiScaledWidth() - 400;
        this.canvas.height = minecraft.getWindow().getGuiScaledHeight();
        this.canvas.offsetX = 200;

        saveButton.offsetX = 204;
        saveButton.offsetY = 4;

        this.refresh(width, height);

        this.lastLayout = root.extractLayout(0, 0, 0);
    }

    private Renderable buildLeftSidebar() {
        List<Renderable> patchSelectButtons = new ArrayList<>();
        List<Renderable> otherButtons = new ArrayList<>();

        for(var graph : this.state.patchGraphs) {
            var button = new ImageButton(this.state.getCurrentGraph() == graph ? ImageButton.SCHEMATIC_BUTTON_ACTIVE : ImageButton.SCHEMATIC_BUTTON, 150, 25, (btn, state) -> {
                for(var el : patchSelectButtons) {
                    if(el instanceof ImageButton b) b.image = ImageButton.SCHEMATIC_BUTTON;
                }

                ((ImageButton) btn).image = ImageButton.SCHEMATIC_BUTTON_ACTIVE;

                this.setGraph(graph);
            });

            button.text = new Text(graph.name, 0xffffffff);

            patchSelectButtons.add(button);
        }

        otherButtons.add(new ImageButton(ImageButton.PLUS, 24, 24, (btn, state) -> {
            PacketDistributor.sendToServer(new CreatePatchServerboundPayload(
                    this.state.controllerPos
            ));
        }));

        var list = new VerticalList<>(patchSelectButtons, 6, false, true);
        list.offsetY = 16;
        list.width = 200;

        var otherButtonsList = new VerticalList<>(otherButtons, 2, false, true);
        list.elements.add(otherButtonsList);

        var scrollable = new Scrollable<>(list, 200, Minecraft.getInstance().getWindow().getGuiScaledHeight());
        return new BackgroundColorNode<>(FastColor.ARGB32.color(200, 25, 25, 35), scrollable);
    }

    public void setGraph(PatchGraph graph) {
        this.state.currentGraph = this.state.patchGraphs.indexOf(graph);
        if(this.state.getCurrentGraph() == null) return;
        this.state.graphNodes.elements.clear();
        this.state.graphNodeToRenderableMap.clear();
        this.state.draggingFrom = null;

        for(var id : graph.nodeDescriptors.keySet()) {
            var descriptor = graph.nodeDescriptors.get(id);
            var node = new RenderableGraphNode(descriptor, id, false);
            var position = graph.nodePositions.get(id);
            if(position == null) {
                graph.nodePositions.put(id, Vec2.ZERO);
                position = Vec2.ZERO;
            }
            node.offsetX = (int) position.x;
            node.offsetY = (int) position.y;
            this.state.graphNodes.elements.add(node);
            this.state.graphNodeToRenderableMap.put(id, node);
        }
    }

    public record Line(List<Vec3> points, int color) {}

    private List<Line> getLines(int mouseX, int mouseY) {
        List<Line> out = new ArrayList<>();

        List<Vec2[]> connections = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        if(this.state.draggingFrom != null) {
            var from = new Vec2(this.state.draggingFrom.absX + 2, this.state.draggingFrom.absY + 2);
            var to = new Vec2(mouseX - 2, mouseY - 2);

            connections.add(new Vec2[] { from, to });
            colors.add(this.state.draggingFrom.type.color);
        }

        if(state.getCurrentGraph() != null) for(var conn : state.getCurrentGraph().connections) {
            var fromNode = state.graphNodeToRenderableMap.get(conn.from());
            var toNode = state.graphNodeToRenderableMap.get(conn.to());
            if(fromNode == null || toNode == null) continue;

            var toPort = toNode.ports.get(conn.keyTo());
            var fromPort = fromNode.ports.get(conn.keyFrom());

            if(toPort == null || fromPort == null) continue;

            connections.add(new Vec2[] {
                    new Vec2(fromPort.port.absX, fromPort.port.absY + 2),
                    new Vec2(toPort.port.absX, toPort.port.absY + 2),
            });
            colors.add(fromPort.port.type.color);
        }


        for(int c = 0; c < connections.size(); c++) {
            var connection = connections.get(c);
            var color = colors.get(c);

            var c1 = new Vec2(connection[0].x + 2, connection[0].y + 2);
            var c4 = new Vec2(connection[1].x + 2, connection[1].y + 2);

            var h = (c1.x + c4.x) * 0.5f;

            var c2 = c1.add(new Vec2(h, c1.y)).scale(0.5f);
            var c3 = c4.add(new Vec2(h, c4.y)).scale(0.5f);

//            c2 = new Vec2(0.0f, 0.0f);
//            c3 = new Vec2(0.0f, 0.0f);

            var b = new Bezier4(c1, c2, c3, c4);

            var evaluated = b.eval(0.02f);

            var line = new ArrayList<Vec3>();

            float dist = 0.0f;

            for(int i = 0; i < evaluated.size() - 1; i++) {
                var p1 = evaluated.get(i);
                var p2 = evaluated.get(i + 1);

                line.add(new Vec3(p1.x, p1.y, dist));
                dist += (float) Math.sqrt(p1.distanceToSqr(p2));
                line.add(new Vec3(p2.x, p2.y, dist));
            }

            out.add(new Line(line, color));
        }
        return out;
    }

    private void drawEffects(GuiGraphics graphics, int mouseX, int mouseY) {
        var lines = this.getLines(mouseX, mouseY);

        if(this.canvas == null) return;
        if(this.canvas.layoutCache == null) return;

        if(lines.isEmpty()) return;

        BezierCurveRenderer.render(graphics, lines);
    }

    @Override
    public void render(@NonNull GuiGraphics graphics, int mouseX, int mouseY, float a) {
        this.drawEffects(graphics, mouseX, mouseY);

        if(this.lastLayout != null) this.lastLayout.paint(graphics);

        if(this.root != null) {
            if(state.editorDirty) {
                this.lastLayout = new Many(List.of(this.root, saveButton)).extractLayout(0, 0, 0);
            } else this.lastLayout = this.root.extractLayout(0, 0, 0);
        }

        int tl = this.leftPos - 8;
        int tp = this.topPos - 9;
        graphics.blit(CONTAINER_TEXTURE, tl, tp, tl + 256, tp + 256, 0, 1, 0, 1);
        super.render(graphics, mouseX, mouseY, a);
    }
}