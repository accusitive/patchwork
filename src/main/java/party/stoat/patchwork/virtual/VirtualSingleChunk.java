package party.stoat.patchwork.virtual;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.ticks.TickContainerAccess;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class VirtualSingleChunk extends LevelChunk {

    private Level realLevel;
    private final BlockPos blockPos;
    private BlockState theState;
    private @Nullable BlockEntity theEntity;

    public VirtualSingleChunk(ChunkPos chunkPos, Level realLevel, BlockPos pos, BlockState state, BlockEntity entity) {
        super(realLevel, chunkPos);

        this.blockPos = pos;
        this.theState = state;
        this.theEntity = entity;
    }

    @Override
    public @Nullable BlockState setBlockState(BlockPos blockPos, BlockState blockState, @Block.UpdateFlags int i) {
        if(blockPos != this.blockPos) return null;

        this.theState = blockState;

        return blockState;
    }

    @Override
    public void setBlockEntity(BlockEntity blockEntity) {
        if(blockPos != this.blockPos) return;

        this.theEntity = blockEntity;
    }

    @Override
    public void addEntity(Entity entity) {
    }

    @Override
    public ChunkStatus getPersistedStatus() {
        return null;
    }

    @Override
    public void removeBlockEntity(BlockPos blockPos) {

    }

    @Override
    public @Nullable CompoundTag getBlockEntityNbtForSaving(BlockPos blockPos, HolderLookup.Provider provider) {
        return null;
    }

    @Override
    public TickContainerAccess<Block> getBlockTicks() {
        return null;
    }

    @Override
    public @NonNull TickContainerAccess<Fluid> getFluidTicks() {
        return null;
    }

    @Override
    public @NonNull PackedTicks getTicksForSerialization(long l) {
        return new PackedTicks(List.of(), List.of());
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos blockPos) {
        return blockPos.equals(this.blockPos) ? this.theEntity : null;
    }

    @Override
    public <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos pos, BlockEntityType<T> type) {
        var be = this.getBlockEntity(pos);

        if(be == null) return Optional.empty();

        if(be.getType() == type) return Optional.of((T) be); else return Optional.empty();
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos, EntityCreationType creationType) {
        return this.getBlockEntity(pos);
    }

    @Override
    public @NonNull BlockState getBlockState(BlockPos blockPos) {
        return blockPos.equals(this.blockPos) ? this.theState : null;
    }

    @Override
    public @NonNull FluidState getFluidState(BlockPos blockPos) {
        return this.realLevel.getFluidState(blockPos);
    }
}
