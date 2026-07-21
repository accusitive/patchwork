package party.stoat.patchwork.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(ServerLevel.class)
public class UpdateClientWhenVirtualizedServerBlockStateChanges {

//    @Inject(method = "blockstate")
//    public void onBlockStateChange() {
//
//    }

}
