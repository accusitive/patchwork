package party.stoat.patchwork.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.SavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import party.stoat.patchwork.MyBlocks;
import party.stoat.patchwork.virtual.LevelVirtualBlockCache;
import party.stoat.patchwork.virtual.MachineLevelSavedData;

import java.util.Map;

@Mixin(Container.class)
public interface ContainerInMachineLevelYupThatsValidYup {

    @Inject(method = "stillValidBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/player/Player;F)Z", at = @At("HEAD"), cancellable = true)
    private static void patchwork$yuupPointsUpThatsValid(BlockEntity blockEntity, Player player, float distanceBuffer, CallbackInfoReturnable<Boolean> cir) {
        if(blockEntity.getLevel() instanceof ServerLevel serverLevel) {
            if(serverLevel.getDataStorage().computeIfAbsent(new SavedData.Factory<>(MachineLevelSavedData::create, MachineLevelSavedData::load), "machine_count").virtualized.contains(blockEntity.getBlockPos())) {
                cir.setReturnValue(true);
            }
        } else if(blockEntity.getLevel() instanceof LevelVirtualBlockCache cache) {
            if(cache.patchwork$getCache().containsKey(new BlockPos(blockEntity.getBlockPos().getX() & (~15), blockEntity.getBlockPos().getY() & (~15), blockEntity.getBlockPos().getZ() & (~15)))) cir.setReturnValue(true);
        }
    }

}
