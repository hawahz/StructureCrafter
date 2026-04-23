package io.github.hawah.structure_crafter.client;

import io.github.hawah.structure_crafter.client.render.ruler.RulerMaker;
import io.github.hawah.structure_crafter.util.StructureData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

public abstract class StructureWandModifier {
    protected Type type;

    protected StructureWandModifier(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        NONE,
        RULER,
    }

    public abstract BlockPos applyModify(BlockPos pos);
    public abstract Direction applyModify(Direction pos);

    public abstract void submit(BlockPos selectPos, Direction direction, StructureData structureData);

    public abstract void onPlace(BlockPos placeAt, Direction direction);

    public abstract void clear();

    public static class DummyModifier extends StructureWandModifier {
        private DummyModifier() {
            super(Type.NONE);
        }
        @Override
        public BlockPos applyModify(BlockPos pos) {
            return pos;
        }
        @Override
        public Direction applyModify(Direction direction) {
            return direction;
        }
        @Override
        public void submit(BlockPos selectPos, Direction direction, StructureData structureData) {
        }
        @Override
        public void onPlace(BlockPos placeAt, Direction direction) {
        }

        @Override
        public void clear() {
        }
    }

    public static class Ruler extends StructureWandModifier {

        private BlockPos anchor = null;
        private Direction direction = null;

        private Ruler() {
            super(Type.RULER);
        }

        private void setAnchor(BlockPos anchor) {
            this.anchor = anchor;
        }

        private BlockPos anchor() {
            return anchor;
        }

        @Override
        public BlockPos applyModify(BlockPos pos) {
            if (anchor == null) {
                return pos;
            }

            int x = pos.getX();
            int z = pos.getZ();
            if (Math.abs(x - anchor().getX()) < Math.abs(z - anchor().getZ())) {
                return new BlockPos(anchor().getX(), pos.getY(), z);
            }
            return new BlockPos(x, pos.getY(), anchor().getZ());
        }

        @Override
        public Direction applyModify(Direction pos) {
            return direction == null? pos: direction;
        }

        @Override
        public void submit(BlockPos selectPos, Direction direction, StructureData structureData) {
            if (selectPos == null || this.anchor == null || direction == null || structureData == null)
                return;
            Vec3i size = structureData.structureTemplate().getSize();
            BlockPos centerOffset = structureData.center();
            BlockPos mul, amul;
            if (direction.equals(Direction.NORTH)) {
                int deltaX = Math.abs(selectPos.getX() - this.anchor.getX());
                // Along Z Axis
                if (deltaX == 0) {
                    centerOffset = new BlockPos(0, centerOffset.getY(), centerOffset.getZ());
                    if (anchor.getZ() < selectPos.getZ()) {
                        mul = anchor().subtract(centerOffset).offset(0, 0, size.getZ()-1);
                        amul = selectPos.subtract(centerOffset);
                    } else {
                        mul = anchor().subtract(centerOffset);
                        amul = selectPos.subtract(centerOffset).offset(0, 0, size.getZ()-1);
                    }
                } else {
                    centerOffset = new BlockPos(centerOffset.getX(), centerOffset.getY(), 0);
                    if (anchor.getX() < selectPos.getX()) {
                        mul = anchor().subtract(centerOffset).offset(size.getX() - 1, 0, 0);
                        amul = selectPos.subtract(centerOffset);
                    } else {
                        mul = anchor().subtract(centerOffset);
                        amul = selectPos.subtract(centerOffset).offset(size.getX() - 1, 0, 0);
                    }
                }
            } else if (direction.equals(Direction.SOUTH)) {
                int deltaX = Math.abs(selectPos.getX() - this.anchor.getX());
                // Along Z Axis
                if (deltaX == 0) {
                    centerOffset = new BlockPos(0, centerOffset.getY(),size.getZ() - 1 -  centerOffset.getZ());
                    if (anchor.getZ() < selectPos.getZ()) {
                        mul = anchor().subtract(centerOffset).offset(0, 0, size.getZ()-1);
                        amul = selectPos.subtract(centerOffset);
                    } else {
                        mul = anchor().subtract(centerOffset);
                        amul = selectPos.subtract(centerOffset).offset(0, 0, size.getZ()-1);
                    }
                } else {
                    centerOffset = new BlockPos(size.getX() - centerOffset.getX() - 1, centerOffset.getY(), 0);
                    if (anchor.getX() < selectPos.getX()) {
                        mul = anchor().subtract(centerOffset).offset(size.getX() - 1, 0, 0);
                        amul = selectPos.subtract(centerOffset);
                    } else {
                        mul = anchor().subtract(centerOffset);
                        amul = selectPos.subtract(centerOffset).offset(size.getX() - 1, 0, 0);
                    }
                }
            } else if (direction.equals(Direction.EAST)) {
                int deltaX = Math.abs(selectPos.getX() - this.anchor.getX());
                // Along Z Axis
                if (deltaX == 0) {
                    centerOffset = new BlockPos(0, centerOffset.getY(), centerOffset.getX());
                    if (anchor.getZ() < selectPos.getZ()) {
                        mul = anchor().subtract(centerOffset).offset(0, 0, size.getX()-1);
                        amul = selectPos.subtract(centerOffset);
                    } else {
                        mul = anchor().subtract(centerOffset);
                        amul = selectPos.subtract(centerOffset).offset(0, 0, size.getX()-1);
                    }
                } else {
                    centerOffset = new BlockPos(size.getZ() - centerOffset.getZ() - 1, centerOffset.getY(), 0);
                    if (anchor.getX() < selectPos.getX()) {
                        mul = anchor().subtract(centerOffset).offset(size.getZ() - 1, 0, 0);
                        amul = selectPos.subtract(centerOffset);
                    } else {
                        mul = anchor().subtract(centerOffset);
                        amul = selectPos.subtract(centerOffset).offset(size.getZ() - 1, 0, 0);
                    }
                }
            } else {
                int deltaX = Math.abs(selectPos.getX() - this.anchor.getX());
                // Along Z Axis
                if (deltaX == 0) {
                    centerOffset = new BlockPos(0, centerOffset.getY(), size.getX() - centerOffset.getX() - 1);
                    if (anchor.getZ() < selectPos.getZ()) {
                        mul = anchor().subtract(centerOffset).offset(0, 0, size.getX()-1);
                        amul = selectPos.subtract(centerOffset);
                    } else {
                        mul = anchor().subtract(centerOffset);
                        amul = selectPos.subtract(centerOffset).offset(0, 0, size.getX()-1);
                    }
                } else {
                    centerOffset = new BlockPos(centerOffset.getZ(), centerOffset.getY(), 0);
                    if (anchor.getX() < selectPos.getX()) {
                        mul = anchor().subtract(centerOffset).offset(size.getZ() - 1, 0, 0);
                        amul = selectPos.subtract(centerOffset);
                    } else {
                        mul = anchor().subtract(centerOffset);
                        amul = selectPos.subtract(centerOffset).offset(size.getZ() - 1, 0, 0);
                    }
                }
            }

            RulerMaker.getInstance().chase(this, amul, mul)
                    .setRGBA(1, 1, 1, 1)
                    .smooth(1.0F)
                    .finish();
        }

        @Override
        public void onPlace(BlockPos placeAt, Direction direction) {
            if (this.anchor != null)
                return;
            this.setAnchor(placeAt);
            this.direction = direction;
        }

        @Override
        public void clear() {
            RulerMaker.getInstance().chase(this)
                    .discard()
                    .finish();
        }


    }

    public static <T extends StructureWandModifier> T create(Type type) {
        return switch (type) {
            case RULER -> (T) new Ruler();
            default -> (T) new DummyModifier();
        };
    }

}
