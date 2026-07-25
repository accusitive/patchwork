package party.stoat.patchwork.virtual;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class VirtualManager {

    public BlockPos allocate(ServerLevel level, UUID uuid, ItemStack stack) {
        if(stack.getItem() instanceof BlockItem blockItem) {
            var data = level.getDataStorage().computeIfAbsent(MachineLevelSavedData.ID);

            int count = data.increment();

            var worldHeight = level.getMaxY() - level.getMinY();
            var maxInChunk = (worldHeight / 5) * 9 - 1;

            var relX = (count % 3) * 4 + 3;
            var relZ = ((count / 3) % 3) * 4 + 3;

            int x = -(level.getWorldBorder().getAbsoluteMaxSize() + 16) + ((count / maxInChunk) * 16);
            int y = (((count / 9) * 5) % worldHeight) + level.getMinY();
            int z = -(level.getWorldBorder().getAbsoluteMaxSize() + 16);

            level.setChunkForced(x / 16, z / 16, true);

            var pos = new BlockPos(x + relX, y, z + relZ);
            data.virtualized.add(pos);

            for(int xD=-2;xD<3;xD++) {
                for(int yD=-2;yD<3;yD++) {
                    for(int zD=-2;zD<3;zD++) {
                        var posD = new BlockPos(
                                xD + pos.getX(), yD + pos.getY(), zD + pos.getZ()
                        );

                        if(xD == -2 || xD == 2 || yD == -2 || yD == 2 || zD == -2 || zD == 2) {
                            level.setBlock(
                                    posD,
                                    Blocks.BEDROCK.defaultBlockState(),
                                    Block.UPDATE_ALL
                            );
                        } else {
                            level.setBlock(posD, Blocks.AIR.defaultBlockState(), Block.UPDATE_NONE);
                        }
                    }
                }
            }

            level.setBlockAndUpdate(pos, blockItem.getBlock().defaultBlockState());

            data.setDirty();
//
//            BlockPos supportPos = pos.below();
//
//            level.setBlockAndUpdate(supportPos, Blocks.BEDROCK.defaultBlockState());
//
//            Vec3 hitLocation = new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
//
//            BlockHitResult hitResult = new BlockHitResult(
//                    hitLocation,
//                    Direction.UP,
//                    supportPos,
//                    false
//            );
//
//            UseOnContext useOnContext = new UseOnContext(
//                    level,
//                    null,
//                    InteractionHand.MAIN_HAND,
//                    stack,
//                    hitResult
//            );
//
//            blockItem.place(new BlockPlaceContext(
//                    useOnContext
//            ));

            return pos;
        } else return null;
    }

}
