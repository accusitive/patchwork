package party.stoat.patchwork.mixin;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import party.stoat.patchwork.virtual.PlayerVirtualTrackedChunk;

@Mixin(ChunkMap.class)
public class ChunkMapTrackPlayerWatchingRemoteMachine {

    @Inject(method = "isChunkTracked", at = @At("RETURN"), cancellable = true)
    public void remotePlayersTrackVirtualBlocks(ServerPlayer player, int chunkX, int chunkZ, CallbackInfoReturnable<Boolean> cir) {
        if(player instanceof PlayerVirtualTrackedChunk tracker && tracker.patchwork$getChunk() != null) {
            boolean virtualIsTracked = tracker.patchwork$getChunk().x() == chunkX && tracker.patchwork$getChunk().z() == chunkZ;
            cir.setReturnValue(cir.getReturnValue() | virtualIsTracked);
        }
    }

}
