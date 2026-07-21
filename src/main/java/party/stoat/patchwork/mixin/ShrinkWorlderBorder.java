package party.stoat.patchwork.mixin;

import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldBorder.class)
public class ShrinkWorlderBorder {

    @Shadow
    private int absoluteMaxSize;

    @Inject(method = "<init>()V", at = @At("RETURN"))
    public void shrinkBorder(CallbackInfo ci) {
        this.absoluteMaxSize = this.absoluteMaxSize - 16;
    }

}
