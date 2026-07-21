package party.stoat.patchwork.virtual;

import net.minecraft.world.level.ChunkPos;
import org.jspecify.annotations.Nullable;

public interface PlayerVirtualTrackedChunk {

    void patchwork$setChunk(@Nullable ChunkPos pos);

    @Nullable ChunkPos patchwork$getChunk();

}
