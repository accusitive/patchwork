package party.stoat.patchwork;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import party.stoat.patchwork.block.SFCable;
import party.stoat.patchwork.block.SFDrive;
import party.stoat.patchwork.block.SFDriveBlockEntity;
import party.stoat.patchwork.block.SFTerminal;
import party.stoat.patchwork.block.controller.SFController;
import party.stoat.patchwork.block.controller.SFControllerBlockEntity;
import party.stoat.patchwork.block.sf_interface.SFInterface;

import java.util.function.Supplier;

public class MyBlocks {
    // Creates a new Block with the id "patchwork:example_block", combining the namespace and path

    public static final DeferredBlock<Block> SF_CABLE = Patchwork.BLOCKS.registerBlock("sf_cable", SFCable::new, BlockBehaviour.Properties::dynamicShape);
    
    public static final DeferredBlock<Block> SF_INTERFACE = Patchwork.BLOCKS.registerBlock("sf_interface", SFInterface::new, props -> props);

    public static final DeferredBlock<Block> SF_TERMINAL = Patchwork.BLOCKS.registerBlock("sf_terminal", SFTerminal::new, props -> props.lightLevel(
            state -> state.getValue(SFTerminal.POWERED) ? 12 : 0
    ).dynamicShape());

    public static final DeferredBlock<Block> SF_CONTROLLER = Patchwork.BLOCKS.registerBlock("sf_controller", SFController::new, props -> props.lightLevel(
                    state -> state.getValue(SFController.POWERED) ? 12 : 0
            ));


    public static final Supplier<BlockEntityType<SFControllerBlockEntity>> SF_CONTROLLER_BLOCK_ENTITY = Patchwork.BLOCK_ENTITY_TYPES.register(
            "sf_controller_entity",
            () -> new BlockEntityType<>(
                    SFControllerBlockEntity::new,
                    false,
                    SF_CONTROLLER.get()
            )
    );

    public static final DeferredBlock<Block> SF_DRIVE = Patchwork.BLOCKS.registerBlock("sf_drive", SFDrive::new, props -> props);

    public static final Supplier<BlockEntityType<SFDriveBlockEntity>> SF_DRIVE_BLOCK_ENTITY = Patchwork.BLOCK_ENTITY_TYPES.register(
            "sf_drive_entity",
            () -> new BlockEntityType<>(
                    SFDriveBlockEntity::new,
                    false,
                    SF_DRIVE.get()
            )
    );

    // Creates a new BlockItem with the id "patchwork:example_block", combining the namespace and path
    public static final DeferredItem<BlockItem> SF_CONTROLLER_ITEM = Patchwork.ITEMS.registerSimpleBlockItem("sf_controller", SF_CONTROLLER);
    public static final DeferredItem<BlockItem> SF_CABLE_ITEM = Patchwork.ITEMS.registerSimpleBlockItem("sf_cable", SF_CABLE);
    public static final DeferredItem<BlockItem> SF_INTERFACE_ITEM = Patchwork.ITEMS.registerSimpleBlockItem("sf_interface", SF_INTERFACE);
    public static final DeferredItem<BlockItem> SF_TERMINAL_ITEM = Patchwork.ITEMS.registerSimpleBlockItem("sf_terminal", SF_TERMINAL);
    public static final DeferredItem<BlockItem> SF_DRIVE_ITEM = Patchwork.ITEMS.registerSimpleBlockItem("sf_drive", SF_DRIVE);

    public static void initialize() { }
}
