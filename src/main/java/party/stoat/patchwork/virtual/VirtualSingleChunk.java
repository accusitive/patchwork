package party.stoat.patchwork.virtual;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickContainerAccess;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nullable;
import java.util.Optional;

public class VirtualSingleChunk extends LevelChunk {

    private final BlockPos blockPos;
    private BlockState theState;
    private @Nullable BlockEntity theEntity;

    public VirtualSingleChunk(ChunkPos chunkPos, Level realLevel, BlockPos pos, BlockState state, @Nullable BlockEntity entity) {
        super(realLevel, chunkPos);

        this.blockPos = pos;
        this.theState = state;
        this.theEntity = entity;
    }

    @Override
    public @Nullable BlockState setBlockState(@NonNull BlockPos blockPos, @NonNull BlockState blockState, boolean bool) {
        if(blockPos != this.blockPos) return null;

        this.theState = blockState;

        return blockState;
    }

    @Override
    public void setBlockEntity(@NonNull BlockEntity blockEntity) {
        if(!blockEntity.getBlockPos().equals(this.blockPos)) return;

        this.theEntity = blockEntity;
    }

    @Override
    public void addEntity(@NonNull Entity entity) {
    }

    @Override
    public @NonNull ChunkStatus getPersistedStatus() {
        return ChunkStatus.FULL;
    }

    @Override
    public void removeBlockEntity(@NonNull BlockPos blockPos) {

    }

    @Override
    public @Nullable CompoundTag getBlockEntityNbtForSaving(@NonNull BlockPos blockPos, HolderLookup.@NonNull Provider provider) {
        return null;
    }

    @Override
    public @NonNull TickContainerAccess<Block> getBlockTicks() {
        return new TickContainerAccess<Block>() {
            @Override
            public void schedule(@NonNull ScheduledTick<Block> scheduledTick) {

            }

            @Override
            public boolean hasScheduledTick(@NonNull BlockPos blockPos, @NonNull Block block) {
                return false;
            }

            @Override
            public int count() {
                return 0;
            }
        };
    }

    @Override
    public @NonNull TickContainerAccess<Fluid> getFluidTicks() {
        return new TickContainerAccess<Fluid>() {
            @Override
            public void schedule(@NonNull ScheduledTick<Fluid> scheduledTick) {

            }

            @Override
            public boolean hasScheduledTick(@NonNull BlockPos blockPos, @NonNull Fluid fluid) {
                return false;
            }

            @Override
            public int count() {
                return 0;
            }
        };
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos blockPos) {
        return blockPos.equals(this.blockPos) ? this.theEntity : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> @NonNull Optional<T> getBlockEntity(@NonNull BlockPos pos, @NonNull BlockEntityType<T> type) {
        var be = this.getBlockEntity(pos);

        if(be == null) return Optional.empty();

        if(be.getType() == type) return Optional.of((T) be); else return Optional.empty();
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(@NonNull BlockPos pos, @NonNull EntityCreationType creationType) {
        return this.getBlockEntity(pos);
    }

    @Override
    public @NonNull BlockState getBlockState(BlockPos blockPos) {
        return blockPos.equals(this.blockPos) ? this.theState : Blocks.AIR.defaultBlockState();
    }

    @Override
    public @NonNull FluidState getFluidState(@NonNull BlockPos blockPos) {
        return Fluids.EMPTY.defaultFluidState();
    }
}
