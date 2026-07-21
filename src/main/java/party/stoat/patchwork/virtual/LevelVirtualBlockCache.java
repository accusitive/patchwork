package party.stoat.patchwork.virtual;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Optional;

public interface LevelVirtualBlockCache {

    void patchwork$cacheBlock(BlockPos blockPos, BlockState state, Optional<CompoundTag> nbt);

    HashMap<BlockPos, VirtualSingleChunk> patchwork$getCache();

}
