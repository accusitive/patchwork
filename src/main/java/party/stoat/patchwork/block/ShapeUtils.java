package party.stoat.patchwork.block;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public final class ShapeUtils {

    private ShapeUtils() {}

    public static Map<Direction, VoxelShape> rotateAll(VoxelShape shape) {
        return Map.of(
                Direction.NORTH, shape,
                Direction.SOUTH, rotateY180(shape),
                Direction.WEST, rotateYCCW(shape),
                Direction.EAST, rotateYCW(shape),
                Direction.UP, rotateXCCW(shape),
                Direction.DOWN, rotateXCW(shape)
        );
    }

    private static VoxelShape rotateYCW(VoxelShape shape) {
        VoxelShape[] buffer = {Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                buffer[0] = Shapes.or(buffer[0],
                        Shapes.create(
                                1 - maxZ, minY, minX,
                                1 - minZ, maxY, maxX
                        )));
        return buffer[0];
    }

    private static VoxelShape rotateYCCW(VoxelShape shape) {
        VoxelShape[] buffer = {Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                buffer[0] = Shapes.or(buffer[0],
                        Shapes.create(
                                minZ, minY, 1 - maxX,
                                maxZ, maxY, 1 - minX
                        )));
        return buffer[0];
    }

    private static VoxelShape rotateY180(VoxelShape shape) {
        VoxelShape[] buffer = {Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                buffer[0] = Shapes.or(buffer[0],
                        Shapes.create(
                                1 - maxX, minY, 1 - maxZ,
                                1 - minX, maxY, 1 - minZ
                        )));
        return buffer[0];
    }

    private static VoxelShape rotateXCW(VoxelShape shape) {
        VoxelShape[] buffer = {Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                buffer[0] = Shapes.or(buffer[0],
                        Shapes.create(
                                minX, 1 - maxZ, minY,
                                maxX, 1 - minZ, maxY
                        )));
        return buffer[0];
    }

    private static VoxelShape rotateXCCW(VoxelShape shape) {
        VoxelShape[] buffer = {Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                buffer[0] = Shapes.or(buffer[0],
                        Shapes.create(
                                minX, minZ, 1 - maxY,
                                maxX, maxZ, 1 - minY
                        )));
        return buffer[0];
    }
}
