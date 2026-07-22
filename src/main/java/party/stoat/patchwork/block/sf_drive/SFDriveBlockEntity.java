package party.stoat.patchwork.block.sf_drive;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import party.stoat.patchwork.MyBlocks;

public class SFDriveBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {

    public NonNullList<ItemStack> slots = NonNullList.withSize(9, ItemStack.EMPTY);

    public SFDriveBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(MyBlocks.SF_DRIVE_BLOCK_ENTITY.get(), worldPosition, blockState);
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        ContainerHelper.saveAllItems(output, slots);

        super.saveAdditional(output);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        ContainerHelper.loadAllItems(input, slots);

        super.loadAdditional(input);
    }

    @Override
    protected @NonNull Component getDefaultName() {
        return Component.translatable("gui.patchwork.sf_drive");
    }

    @Override
    protected @NonNull NonNullList<ItemStack> getItems() {
        return slots;
    }

    @Override
    protected void setItems(@NonNull NonNullList<ItemStack> nonNullList) {
        this.slots = nonNullList;
    }

    @Override
    protected @NonNull AbstractContainerMenu createMenu(int i, @NonNull Inventory inventory) {
        return new DispenserMenu(i, inventory, this);
    }

    @Override
    public int getContainerSize() {
        return 9;
    }

    @Override
    public int @NonNull [] getSlotsForFace(@NonNull Direction direction) {
        return new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
    }

    @Override
    public boolean canPlaceItemThroughFace(int i, @NonNull ItemStack itemStack, @Nullable Direction direction) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int i, @NonNull ItemStack itemStack, @NonNull Direction direction) {
        return true;
    }
}
