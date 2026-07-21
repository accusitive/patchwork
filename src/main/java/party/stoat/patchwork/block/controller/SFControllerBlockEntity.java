package party.stoat.patchwork.block.controller;

import com.google.gson.Gson;
import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.util.NodePos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.ticks.ContainerSingleItem;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import party.stoat.patchwork.MyBlocks;
import party.stoat.patchwork.Patchwork;
import party.stoat.patchwork.block.ControllerConfiguration;
import party.stoat.patchwork.block.SFControllerMenu;
import party.stoat.patchwork.block.SFEnergyHandler;
import party.stoat.patchwork.graph.*;
import party.stoat.patchwork.graphlib.SFControllerNode;
import party.stoat.patchwork.network.SFControllerSyncClientboundPayload;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class SFControllerBlockEntity extends BlockEntity implements MenuProvider, ContainerSingleItem, WorldlyContainer {

    private ItemStack theItem = ItemStack.EMPTY;
    private List<ItemStack> spawnIn = new ArrayList<>();

    public ControllerConfiguration config = new ControllerConfiguration();

    public SimpleEnergyHandler storage = new SimpleEnergyHandler(1000000, 1000000, 1000000);

    public HashSet<BlockPos> loaded = new HashSet<>();

    public ServerPlayer watcher;

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, SFControllerBlockEntity entity) {
        ServerLevel serverLevel;
        if(level instanceof ServerLevel s) {
            serverLevel = s;
        } else return;

        var machineLevel = level.getServer().getLevel(MyBlocks.MACHINE_LEVEL);

        for(var instance : entity.config.instances.values()) {
            for(var node : instance.nodes.values()) {
                if(node instanceof VirtualizedBlockNode virtual) {
                    if(!entity.loaded.contains(virtual.proxyPos)) {
                        machineLevel.setChunkForced(virtual.proxyPos.getX() / 16, virtual.proxyPos.getZ() / 16, true);
                    }
                }
            }
        }

        entity.config.initializeIfNeeded(serverLevel.getServer());

        var thisNode = new NodePos(entity.worldPosition, SFControllerNode.INSTANCE);

        BlockGraph sfNetworkGraph = Patchwork.UNIVERSE.getGraphWorld(serverLevel).getGraphForNode(thisNode);

        if(sfNetworkGraph != null) outer: try(Transaction transaction = Transaction.openRoot()) {
            for(var sfNode : sfNetworkGraph.getNodes().toList()) {
                if(sfNode.getBlockState().getBlock() instanceof SFEnergyHandler energyHandler) {
                    var desired = energyHandler.desiredAmount();
                    var extractedAmount = entity.storage.extract(desired, transaction);

                    if(extractedAmount < desired) break outer;

                    energyHandler.insert(extractedAmount, transaction);
                }
            }

            transaction.commit();
        }

        if(sfNetworkGraph != null) for(var sfNode : sfNetworkGraph.getNodes().toList()) {
            if(sfNode.getBlockState().getBlock() instanceof SFEnergyHandler energyHandler) {
                energyHandler.checkPowered(sfNode);
            }
        }

        if(entity.storage.getAmountAsLong() > 0) {
            if(!blockState.getValue(SFController.POWERED)) {
                level.setBlockAndUpdate(blockPos, blockState.setValue(SFController.POWERED, true));
            }
        } else {
            if (blockState.getValue(SFController.POWERED)) {
                level.setBlockAndUpdate(blockPos, blockState.setValue(SFController.POWERED, false));
            }
        }

        if(!entity.spawnIn.isEmpty()) {
            for(ItemStack stack : entity.spawnIn) {
                var id = UUID.randomUUID();

                var pos = Patchwork.VIRTUAL_MANAGER.allocate(machineLevel, id, stack);

                entity.config.virtualized.add(pos);
            }
        }

//        if(entity.storage.amount < cost) {
//            entity.storage.amount = 0;
//        }

//        if(machineLevel != null && entity.storage.amount >= cost) {
        if(machineLevel != null && entity.config != null) {
            var nodeGraph = Patchwork.UNIVERSE.getGraphWorld(serverLevel).getGraphForNode(new NodePos(blockPos, SFControllerNode.INSTANCE));
//            entity.storage.amount -= cost;


            outer: try(Transaction transaction = Transaction.openRoot()) {
                for(var patchInstance : entity.config.instances.values()) {
                    for(var node : patchInstance.nodes.values()) {
                        try(Transaction inner = Transaction.open(transaction)) {
                            node.tick(entity.config, patchInstance, serverLevel, nodeGraph, inner, entity);
                            var amount = entity.storage.getAmountAsInt() - 10;
                            entity.storage.set(Math.max(amount, 0));
//                            var amount = 1;

                            if(amount >= 0) inner.commit();
                            else {
                                break outer;
                            }
                        }
                    }
                }

                transaction.commit();
            }
        }

        if(!entity.spawnIn.isEmpty() && entity.watcher != null) {
            var descriptors = entity.config.getNodesFromNetworkResources(Patchwork.UNIVERSE.getGraphWorld(serverLevel).getGraphForNode(
                    thisNode
            ), serverLevel.getServer());

            PacketDistributor.sendToPlayer(entity.watcher, new SFControllerSyncClientboundPayload(new Gson().toJson(entity.config.graphs), new Gson().toJson(descriptors), blockPos));
        }
        entity.spawnIn.clear();
    }

    @Override
    public @NonNull ItemStack getTheItem() {
        return theItem;
    }

    @Override
    public void setTheItem(@NonNull ItemStack itemStack) {
        this.spawnIn.add(itemStack.copyAndClear());
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack itemStack, @Nullable Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack itemStack, Direction direction) {
        return false;
    }

    public static class ExternalStorage {
        public BlockPos pos;

    }

//    public Object2IntMap<Item> reserves = new Object2IntArrayMap<>();

    public SFControllerBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(MyBlocks.SF_CONTROLLER_BLOCK_ENTITY.get(), worldPosition, blockState);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        this.config.save(output.child("config"));
        ContainerHelper.saveAllItems(output, NonNullList.of(theItem));

        super.saveAdditional(output);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setChanged() {
        super.setChanged();

        if(level == null) return;

        BlockState state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        input.child("config").ifPresentOrElse(
                config -> {
                    this.config = ControllerConfiguration.load(config);
                },
                () -> this.config = new ControllerConfiguration()
        );

        ContainerHelper.loadAllItems(input, NonNullList.of(theItem));

        super.loadAdditional(input);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.patchwork.patch_controller");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new SFControllerMenu(containerId, inventory, this);
    }
}
