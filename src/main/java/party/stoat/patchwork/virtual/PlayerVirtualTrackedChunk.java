package party.stoat.patchwork.virtual;

import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nullable;

public interface PlayerVirtualTrackedChunk {

    void patchwork$setChunk(@Nullable ChunkPos pos);

    @Nullable
    ChunkPos patchwork$getChunk();

}
