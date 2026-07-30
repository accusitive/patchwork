package party.stoat.patchwork;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import party.stoat.patchwork.block.SFTerminal;
import party.stoat.patchwork.block.sf_cable.SFCable;
import party.stoat.patchwork.block.sf_controller.SFController;
import party.stoat.patchwork.block.sf_controller.SFControllerBlockEntity;
import party.stoat.patchwork.block.sf_drive.SFDrive;
import party.stoat.patchwork.block.sf_drive.SFDriveBlockEntity;
import party.stoat.patchwork.block.sf_interface.SFInterface;

import java.util.function.Supplier;

public class MyBlocks {

    public static final DeferredBlock<SFCable> SF_CABLE = Patchwork.BLOCKS.register("sf_cable", () -> new SFCable(BlockBehaviour.Properties.of().dynamicShape().strength(0.25F)));

    public static final DeferredBlock<SFInterface> SF_INTERFACE = Patchwork.BLOCKS.register("sf_interface", () -> new SFInterface(BlockBehaviour.Properties.of().strength(0.15F).sound(SoundType.GLASS)));

    public static final DeferredBlock<SFTerminal> SF_TERMINAL = Patchwork.BLOCKS.register("sf_terminal", () -> new SFTerminal(BlockBehaviour.Properties.of().strength(0.8F).lightLevel(state -> state.getValue(SFTerminal.POWERED) ? 12 : 0)));

    public static final DeferredBlock<SFController> SF_CONTROLLER = Patchwork.BLOCKS.register("sf_controller", () -> new SFController(BlockBehaviour.Properties.of().strength(1.0F).sound(SoundType.NETHERITE_BLOCK).lightLevel(state -> state.getValue(SFController.POWERED) ? 12 : 0)));

    public static final DeferredBlock<SFDrive> SF_DRIVE = Patchwork.BLOCKS.register("sf_drive", () -> new SFDrive(BlockBehaviour.Properties.of().strength(0.8F)));

    // TODO: should null be used to build block entities?
    public static final Supplier<BlockEntityType<SFControllerBlockEntity>> SF_CONTROLLER_BLOCK_ENTITY = Patchwork.BLOCK_ENTITY_TYPES.register("sf_controller_entity", () -> BlockEntityType.Builder.of(SFControllerBlockEntity::new, SF_CONTROLLER.get()).build(null));

    public static final Supplier<BlockEntityType<SFDriveBlockEntity>> SF_DRIVE_BLOCK_ENTITY = Patchwork.BLOCK_ENTITY_TYPES.register("sf_drive_entity", () -> BlockEntityType.Builder.of(SFDriveBlockEntity::new, SF_DRIVE.get()).build(null));

    public static final DeferredItem<BlockItem> SF_CONTROLLER_ITEM =
            Patchwork.ITEMS.registerSimpleBlockItem("sf_controller", SF_CONTROLLER);

    public static final DeferredItem<BlockItem> SF_CABLE_ITEM =
            Patchwork.ITEMS.registerSimpleBlockItem("sf_cable", SF_CABLE);

    public static final DeferredItem<BlockItem> SF_INTERFACE_ITEM =
            Patchwork.ITEMS.registerSimpleBlockItem("sf_interface", SF_INTERFACE);

    public static final DeferredItem<BlockItem> SF_TERMINAL_ITEM =
            Patchwork.ITEMS.registerSimpleBlockItem("sf_terminal", SF_TERMINAL);

    public static final DeferredItem<BlockItem> SF_DRIVE_ITEM =
            Patchwork.ITEMS.registerSimpleBlockItem("sf_drive", SF_DRIVE);

    public static void initialize() {
    }
}