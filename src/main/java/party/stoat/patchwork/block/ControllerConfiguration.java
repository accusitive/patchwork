package party.stoat.patchwork.block;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.kneelawk.graphlib.api.graph.BlockGraph;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.factory.TileEntityItemStackChemicalToItemStackFactory;
import mekanism.common.tile.factory.TileEntityItemStackToItemStackFactory;
import mekanism.common.tile.machine.*;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import party.stoat.patchwork.Patchwork;
import party.stoat.patchwork.block.sf_interface.SFInterface;
import party.stoat.patchwork.graph.NodeDescriptor;
import party.stoat.patchwork.graph.PatchGraph;
import party.stoat.patchwork.graphlib.SFInterfaceNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class ControllerConfiguration {

    public HashMap<UUID, PatchInstance> instances = new HashMap<>();
    public List<PatchGraph> graphs = new ArrayList<>();
    public boolean initialized = false;
    public List<BlockPos> virtualized = new ArrayList<>();

    static HashMap<Class<? extends BlockEntity>, BlockConfigurator> configurators = new HashMap<>();
    static HashMap<Class<? extends BlockEntity>, NodeDescriptorProvider> descriptorProvider = new HashMap<>();

    static {
        configurators.put(
                TileEntityChemicalTank.class,
                new MekanismConfigurator()
                        .config(TransmissionType.CHEMICAL)
                        .set(DataType.INPUT, Direction.UP)
                        .set(DataType.OUTPUT, Direction.DOWN)
                        .finish()
        );

        descriptorProvider.put(
                TileEntityChemicalTank.class,
                (config, block, formatter) -> new NodeDescriptor(
                        formatter.apply(block.getName().getString()),
                        List.of(
                                new NodeDescriptor.IO("In", "in", new NodeDescriptor.Data(NodeDescriptor.DataType.Chemical, false), Direction.UP)
                        ),
                        List.of(
                                new NodeDescriptor.IO("Out", "out", new NodeDescriptor.Data(NodeDescriptor.DataType.Chemical, false), Direction.DOWN)
                        ),
                        ARGB.color(255, 110, 100, 105),
                        Identifier.fromNamespaceAndPath(Patchwork.MOD_ID, "virtual"),
                        BuiltInRegistries.ITEM.getKey(BlockItem.BY_BLOCK.get(block)),
                        config
                )
        );

        configurators.put(
                TileEntityFluidTank.class,
                new MekanismConfigurator()
                        .config(TransmissionType.FLUID)
                        .set(DataType.INPUT, Direction.UP)
                        .set(DataType.OUTPUT, Direction.DOWN)
                        .finish()
        );

        descriptorProvider.put(
                TileEntityFluidTank.class,
                (config, block, formatter) -> new NodeDescriptor(
                        formatter.apply(block.getName().getString()),
                        List.of(
                                new NodeDescriptor.IO("In", "in", new NodeDescriptor.Data(NodeDescriptor.DataType.Fluid, false), Direction.UP)
                        ),
                        List.of(
                                new NodeDescriptor.IO("Out", "out", new NodeDescriptor.Data(NodeDescriptor.DataType.Fluid, false), Direction.DOWN)
                        ),
                        ARGB.color(255, 110, 100, 105),
                        Identifier.fromNamespaceAndPath(Patchwork.MOD_ID, "virtual"),
                        BuiltInRegistries.ITEM.getKey(BlockItem.BY_BLOCK.get(block)),
                        config
                )
        );

        descriptorProvider.put(
                AbstractFurnaceBlockEntity.class,
                (config, block, formatter) ->
                        new NodeDescriptor(
                                formatter.apply(block.getName().getString()),
                                List.of(
                                        new NodeDescriptor.IO("In", "in", new NodeDescriptor.Data(NodeDescriptor.DataType.Item, false), Direction.UP),
                                        new NodeDescriptor.IO("Fuel", "fuelin", new NodeDescriptor.Data(NodeDescriptor.DataType.Item, false), Direction.NORTH)
                                ), List.of(
                                new NodeDescriptor.IO("Out", "out", new NodeDescriptor.Data(NodeDescriptor.DataType.Item, false), Direction.DOWN)
                        ),
                                ARGB.color(255, 40, 40, 40),
                                Identifier.fromNamespaceAndPath(Patchwork.MOD_ID, "virtual"),
                                BuiltInRegistries.ITEM.getKey(BlockItem.BY_BLOCK.get(block)),
                                config
                        )
        );

        configurators.put(
                TileEntityElectrolyticSeparator.class,
                new MekanismConfigurator()
                        .config(TransmissionType.CHEMICAL)
                            .set(DataType.OUTPUT_1, Direction.WEST)
                            .set(DataType.OUTPUT_2, Direction.EAST)
                            .finish()
                        .config(TransmissionType.FLUID)
                            .set(DataType.INPUT, Direction.NORTH)
                            .finish()
                        .config(TransmissionType.ENERGY)
                        .set(DataType.INPUT, Direction.UP)
                        .finish()
        );

        descriptorProvider.put(
                TileEntityElectrolyticSeparator.class,
                (config, block, formatter) -> new NodeDescriptor(
                        formatter.apply(block.getName().getString()),
                        List.of(
                                new NodeDescriptor.IO("In", "in", new NodeDescriptor.Data(NodeDescriptor.DataType.Fluid, false), Direction.NORTH),
                                new NodeDescriptor.IO("Power", "powerin", new NodeDescriptor.Data(NodeDescriptor.DataType.Energy, false), Direction.UP)
                        ),
                        List.of(
                                new NodeDescriptor.IO("Left", "left", new NodeDescriptor.Data(NodeDescriptor.DataType.Chemical, false), Direction.WEST),
                                new NodeDescriptor.IO("Right", "right", new NodeDescriptor.Data(NodeDescriptor.DataType.Chemical, false), Direction.EAST)
                        ),
                        ARGB.color(255, 110, 100, 105),
                        Identifier.fromNamespaceAndPath(Patchwork.MOD_ID, "virtual"),
                        BuiltInRegistries.ITEM.getKey(BlockItem.BY_BLOCK.get(block)),
                        config
                )
        );

        configurators.put(
                TileEntityChemicalDissolutionChamber.class,
                new MekanismConfigurator()
                        .config(TransmissionType.CHEMICAL)
                        .set(DataType.INPUT, Direction.UP)
                        .set(DataType.OUTPUT, Direction.SOUTH)
                        .finish()
                        .config(TransmissionType.ITEM)
                        .set(DataType.INPUT, Direction.NORTH)
                        .finish()
        );

        descriptorProvider.put(
                TileEntityChemicalDissolutionChamber.class,
                (config, block, formatter) -> new NodeDescriptor(
                        formatter.apply(block.getName().getString()),
                        List.of(
                                new NodeDescriptor.IO("Item In", "itemin", new NodeDescriptor.Data(NodeDescriptor.DataType.Item, false), Direction.NORTH),
                                new NodeDescriptor.IO("Chemical In", "chemin", new NodeDescriptor.Data(NodeDescriptor.DataType.Chemical, false), Direction.UP),
                                new NodeDescriptor.IO("Power", "powerin", new NodeDescriptor.Data(NodeDescriptor.DataType.Energy, false), Direction.UP)
                        ),
                        List.of(
                                new NodeDescriptor.IO("Out", "out", new NodeDescriptor.Data(NodeDescriptor.DataType.Chemical, false), Direction.SOUTH)
                        ),
                        ARGB.color(255, 110, 100, 105),
                        Identifier.fromNamespaceAndPath(Patchwork.MOD_ID, "virtual"),
                        BuiltInRegistries.ITEM.getKey(BlockItem.BY_BLOCK.get(block)),
                        config
                )
        );

        BlockConfigurator electricMachineConfigurator = new MekanismConfigurator()
                .config(TransmissionType.ITEM)
                .set(DataType.INPUT, Direction.NORTH)
                .set(DataType.OUTPUT, Direction.SOUTH)
                .finish()
                .config(TransmissionType.ENERGY)
                .set(DataType.INPUT, Direction.UP)
                .finish();

        NodeDescriptorProvider electricMachineDescriptor = (config, block, formatter) -> new NodeDescriptor(
                formatter.apply(block.getName().getString()),
                List.of(
                        new NodeDescriptor.IO("In", "in", new NodeDescriptor.Data(NodeDescriptor.DataType.Item, false), Direction.NORTH),
                        new NodeDescriptor.IO("Power", "powerin", new NodeDescriptor.Data(NodeDescriptor.DataType.Energy, false), Direction.UP)
                ),
                List.of(
                        new NodeDescriptor.IO("Out", "out", new NodeDescriptor.Data(NodeDescriptor.DataType.Item, false), Direction.SOUTH)
                ),
                ARGB.color(255, 110, 100, 105),
                Identifier.fromNamespaceAndPath(Patchwork.MOD_ID, "virtual"),
                BuiltInRegistries.ITEM.getKey(BlockItem.BY_BLOCK.get(block)),
                config
        );

        configurators.put(
                TileEntityElectricMachine.class,
                electricMachineConfigurator
        );

        configurators.put(
                TileEntityItemStackToItemStackFactory.class,
                electricMachineConfigurator
        );

        descriptorProvider.put(
                TileEntityElectricMachine.class,
                electricMachineDescriptor
        );

        descriptorProvider.put(
                TileEntityItemStackToItemStackFactory.class,
                electricMachineDescriptor
        );

        configurators.put(
                TileEntityChemicalWasher.class,
                new MekanismConfigurator()
                        .config(TransmissionType.CHEMICAL)
                            .set(DataType.INPUT, Direction.EAST)
                            .set(DataType.OUTPUT, Direction.SOUTH)
                            .finish()
                        .config(TransmissionType.FLUID)
                            .set(DataType.INPUT, Direction.WEST)
                            .finish()
                        .config(TransmissionType.ENERGY)
                            .set(DataType.INPUT, Direction.UP)
                            .finish()
        );

        descriptorProvider.put(
                TileEntityChemicalWasher.class,
                (config, block, formatter) -> new NodeDescriptor(
                        formatter.apply(block.getName().getString()),
                        List.of(
                                new NodeDescriptor.IO("Fluid In", "fluidin", new NodeDescriptor.Data(NodeDescriptor.DataType.Fluid, false), Direction.WEST),
                                new NodeDescriptor.IO("Chemical In", "chemicalin", new NodeDescriptor.Data(NodeDescriptor.DataType.Chemical, false), Direction.EAST),
                                new NodeDescriptor.IO("Power", "powerin", new NodeDescriptor.Data(NodeDescriptor.DataType.Energy, false), Direction.UP)
                        ),
                        List.of(
                                new NodeDescriptor.IO("Chemical Out", "out", new NodeDescriptor.Data(NodeDescriptor.DataType.Chemical, false), Direction.SOUTH)
                        ),
                        ARGB.color(255, 110, 100, 105),
                        Identifier.fromNamespaceAndPath(Patchwork.MOD_ID, "virtual"),
                        BuiltInRegistries.ITEM.getKey(BlockItem.BY_BLOCK.get(block)),
                        config
                )
        );

        configurators.put(
                TileEntityChemicalCrystallizer.class,
                new MekanismConfigurator()
                        .config(TransmissionType.CHEMICAL)
                            .set(DataType.INPUT, Direction.WEST)
                            .finish()
                        .config(TransmissionType.ITEM)
                            .set(DataType.OUTPUT, Direction.EAST)
                            .finish()
                        .config(TransmissionType.ENERGY)
                            .set(DataType.INPUT, Direction.UP)
                        .finish()
        );

        descriptorProvider.put(
                TileEntityChemicalCrystallizer.class,
                (config, block, formatter) -> new NodeDescriptor(
                        formatter.apply(block.getName().getString()),
                        List.of(
                                new NodeDescriptor.IO("In", "in", new NodeDescriptor.Data(NodeDescriptor.DataType.Chemical, false), Direction.WEST),
                                new NodeDescriptor.IO("Power", "powerin", new NodeDescriptor.Data(NodeDescriptor.DataType.Energy, false), Direction.UP)
                        ),
                        List.of(
                                new NodeDescriptor.IO("Out", "out", new NodeDescriptor.Data(NodeDescriptor.DataType.Item, false), Direction.EAST)
                        ),
                        ARGB.color(255, 110, 100, 105),
                        Identifier.fromNamespaceAndPath(Patchwork.MOD_ID, "virtual"),
                        BuiltInRegistries.ITEM.getKey(BlockItem.BY_BLOCK.get(block)),
                        config
                )
        );

        BlockConfigurator advancedElectricMachineConfigurator = new MekanismConfigurator()
                .config(TransmissionType.CHEMICAL)
                .set(DataType.INPUT, Direction.EAST)
                .finish()
                .config(TransmissionType.ITEM)
                .set(DataType.INPUT, Direction.WEST)
                .set(DataType.OUTPUT, Direction.SOUTH)
                .finish()
                .config(TransmissionType.ENERGY)
                .set(DataType.INPUT, Direction.UP)
                .finish();

        NodeDescriptorProvider advancedElectricMachineDescriptor = (config, block, formatter) -> new NodeDescriptor(
                formatter.apply(block.getName().getString()),
                List.of(
                        new NodeDescriptor.IO("Item In", "in", new NodeDescriptor.Data(NodeDescriptor.DataType.Item, false), Direction.WEST),
                        new NodeDescriptor.IO("Chemical In", "chemin", new NodeDescriptor.Data(NodeDescriptor.DataType.Chemical, false), Direction.EAST),
                        new NodeDescriptor.IO("Power", "powerin", new NodeDescriptor.Data(NodeDescriptor.DataType.Energy, false), Direction.UP)
                ),
                List.of(
                        new NodeDescriptor.IO("Out", "out", new NodeDescriptor.Data(NodeDescriptor.DataType.Item, false), Direction.SOUTH)
                ),
                ARGB.color(255, 110, 100, 105),
                Identifier.fromNamespaceAndPath(Patchwork.MOD_ID, "virtual"),
                BuiltInRegistries.ITEM.getKey(BlockItem.BY_BLOCK.get(block)),
                config
        );

        configurators.put(
                TileEntityAdvancedElectricMachine.class,
                advancedElectricMachineConfigurator
        );

        configurators.put(
                TileEntityItemStackChemicalToItemStackFactory.class,
                advancedElectricMachineConfigurator
        );

        descriptorProvider.put(
                TileEntityAdvancedElectricMachine.class,
                advancedElectricMachineDescriptor
        );

        descriptorProvider.put(
                TileEntityItemStackChemicalToItemStackFactory.class,
                advancedElectricMachineDescriptor
        );

        descriptorProvider.put(
                ChestBlockEntity.class,
                (config, block, formatter) -> new NodeDescriptor(
                        formatter.apply(block.getName().getString()),
                        List.of(
                                new NodeDescriptor.IO("In", "in", new NodeDescriptor.Data(NodeDescriptor.DataType.Item, false), Direction.UP)
                        ),
                        List.of(
                                new NodeDescriptor.IO("Out", "out", new NodeDescriptor.Data(NodeDescriptor.DataType.Item, false), Direction.DOWN)
                        ),
                        ARGB.color(255, 110, 100, 105),
                        Identifier.fromNamespaceAndPath(Patchwork.MOD_ID, "virtual"),
                        BuiltInRegistries.ITEM.getKey(BlockItem.BY_BLOCK.get(block)),
                        config
                )
        );
    }

    interface BlockConfigurator {

        void apply(BlockPos pos, BlockState state, BlockEntity entity, ServerLevel level, ServerPlayer player);

    }

    interface NodeDescriptorProvider {

        NodeDescriptor apply(String posConfig, Block state, Function<String, String> formatter);

    }

    public void save(ValueOutput output) {
        var virts = output.childrenList("virtualized");

        for (var pos : virtualized) {
            virts.addChild().putLong("pos", pos.asLong());
        }

        output.putString("graphs", new Gson().toJson(this.graphs));
    }

    public NodeDescriptor getDescriptorForBlock(ServerLevel level, BlockPos pos, Function<String, String> formatter, ServerPlayer player) {
        BlockState state = level.getBlockState(pos);
        BlockEntity entity = level.getBlockEntity(pos);

        var posConfiguration = new Gson().toJson(pos);

        for(var nodeDescriptorClass : descriptorProvider.keySet()) {
            if(nodeDescriptorClass.isInstance(entity)) {
                return descriptorProvider.get(nodeDescriptorClass).apply(new Gson().toJson(pos), state.getBlock(), p -> p);
            }
        }

        return new NodeDescriptor(
                formatter.apply(state.getBlock().getName().getString()),
                List.of(
                ),
                List.of(
                ),
                ARGB.color(255, 110, 100, 105),
                Identifier.fromNamespaceAndPath(Patchwork.MOD_ID, "virtual"),
                BuiltInRegistries.ITEM.getKey(BlockItem.BY_BLOCK.get(state.getBlock())),
                posConfiguration
        );
    }

    public List<NodeDescriptor> getNodesFromNetworkResources(BlockGraph graph, ServerLevel level, ServerPlayer player) {
        List<NodeDescriptor> nodes = new ArrayList<>();

        for (var virtualizedPos : virtualized) {
            nodes.add(configureBlockAndGetDescriptor(level, player, virtualizedPos, s -> s));
        }

        for (var node : graph.getNodes().toList()) {
            if (node.getNode() instanceof SFInterfaceNode) {
                var facing = node.getBlockState().getValue(SFInterface.FACING);

                var proxiedPos = node.getBlockPos().relative(facing);

                nodes.add(configureBlockAndGetDescriptor(level, player, proxiedPos, s -> "Interface (" + s + ")"));
            }
        }

        return nodes;
    }

    private NodeDescriptor configureBlockAndGetDescriptor(ServerLevel level, ServerPlayer player, BlockPos proxiedPos, Function<String, String> formatter) {
        BlockState state = level.getBlockState(proxiedPos);
        BlockEntity entity = level.getBlockEntity(proxiedPos);

        for(var configuratorClass : configurators.keySet()) {
            if(configuratorClass.isInstance(entity)) {
                configurators.get(configuratorClass).apply(proxiedPos, state, entity, level, player);
                break;
            }
        }

        return getDescriptorForBlock(level, proxiedPos, formatter, player);
    }

    public void initializeIfNeeded(MinecraftServer server) {
        if (this.initialized) return;

        for (var graph : this.graphs) {
            var instance = PatchInstance.build(graph);
            instance.initialize(server);
            this.instances.put(graph.graphId, instance);
        }

        this.initialized = true;
    }

    public static ControllerConfiguration load(ValueInput input) {
        var controllerConfig = new ControllerConfiguration();

        var virtualized = input.childrenList("virtualized").get();

        for (var virt : virtualized) {
            var pos = BlockPos.of(virt.getLong("pos").get());
            controllerConfig.virtualized.add(pos);
        }

        controllerConfig.graphs = new Gson().fromJson(input.getString("graphs").get(), new TypeToken<List<PatchGraph>>() {
        }.getType());

        return controllerConfig;
    }

}
