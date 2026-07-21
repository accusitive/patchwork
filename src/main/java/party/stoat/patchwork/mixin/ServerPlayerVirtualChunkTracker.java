package party.stoat.patchwork.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import party.stoat.patchwork.virtual.PlayerVirtualTrackedChunk;

@Mixin(ServerPlayer.class)
public class ServerPlayerVirtualChunkTracker implements PlayerVirtualTrackedChunk {

    @Unique
    private @Nullable ChunkPos remoteChunk;

    @Override
    public void patchwork$setChunk(@Nullable ChunkPos pos) {
        this.remoteChunk = pos;
    }

    @Override
    public @Nullable ChunkPos patchwork$getChunk() {
        return this.remoteChunk;
    }
}
