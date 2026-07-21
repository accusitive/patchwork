package party.stoat.patchwork.virtual;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class VirtualManager {

    public BlockPos allocate(ServerLevel level, UUID uuid, ItemStack stack) {
        if(stack.getItem() instanceof BlockItem blockItem) {
            var data = level.getDataStorage().computeIfAbsent(MachineLevelSavedData.ID);

            int count = data.increment();
            int x = (count % 16) * 160;
            int y = 0;
            int z = (count / 16) * 160;

            level.setChunkForced(x / 16, z / 16, true);

            var pos = new BlockPos(x, y, z);

            level.setBlockAndUpdate(pos, blockItem.getBlock().defaultBlockState());

            return pos;
        } else return null;
    }

}
