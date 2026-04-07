package io.github.hawah.structure_crafter.util;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VoxelShapeMaker {

    public static VoxelShape getByHorizontalDirection(Direction direction, VoxelShape voxelShape) {
        if (voxelShape.isEmpty()) {
            return Shapes.empty();
        }

        return switch (direction) {
            case NORTH -> voxelShape;
            case SOUTH -> rotateShape(voxelShape, 180);
            case EAST -> rotateShape(voxelShape, 90);
            case WEST -> rotateShape(voxelShape, -90);
            default -> voxelShape;
        };
    }

    private static VoxelShape rotateShape(VoxelShape shape, int angle) {
        final VoxelShape[] result = {Shapes.empty()};

        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double rotatedMinX, rotatedMinZ, rotatedMaxX, rotatedMaxZ;

            switch (angle) {
                case 90 -> {
                    rotatedMinX = 1.0 - maxZ;
                    rotatedMaxX = 1.0 - minZ;
                    rotatedMinZ = minX;
                    rotatedMaxZ = maxX;
                }
                case -90 -> {
                    rotatedMinX = minZ;
                    rotatedMaxX = maxZ;
                    rotatedMinZ = 1.0 - maxX;
                    rotatedMaxZ = 1.0 - minX;
                }
                case 180 -> {
                    rotatedMinX = 1.0 - maxX;
                    rotatedMaxX = 1.0 - minX;
                    rotatedMinZ = 1.0 - maxZ;
                    rotatedMaxZ = 1.0 - minZ;
                }
                default -> {
                    rotatedMinX = minX;
                    rotatedMaxX = maxX;
                    rotatedMinZ = minZ;
                    rotatedMaxZ = maxZ;
                }
            }

            VoxelShape box = Shapes.box(rotatedMinX, minY, rotatedMinZ, rotatedMaxX, maxY, rotatedMaxZ);
            result[0] = Shapes.join(result[0], box, BooleanOp.OR);
        });

        return result[0].optimize();
    }

}
