package party.stoat.patchwork.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import party.stoat.patchwork.virtual.MachineLevelSavedData;

@Mixin(LevelChunk.class)
public class LevelChunkTickMustTickVirtualized {

    @Shadow
    @Final
    private Level level;

    @Inject(method = "isTicking", at = @At("RETURN"), cancellable = true)
    public void tickTheSillyBlud(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if(cir.getReturnValue() == false) {
            if(this.level instanceof ServerLevel serverLevel) {
                cir.setReturnValue(
                        serverLevel.getDataStorage().computeIfAbsent(MachineLevelSavedData.ID).virtualized.contains(pos)
                );
            }
        }
    }

}
