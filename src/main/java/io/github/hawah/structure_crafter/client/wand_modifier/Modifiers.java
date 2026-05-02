package io.github.hawah.structure_crafter.client.wand_modifier;

import io.github.hawah.structure_crafter.util.MutablePair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.joml.Vector2i;

public interface Modifiers<T> {
    T modify(T t);
    void onPlace(BlockPos placeAt, Direction direction);
    void setAnchor(BlockPos t);
    BlockPos anchor();
    int priority();
    default Vector2i size() {
        return new Vector2i(-1);
    }
    default Vector2i offset() {
        return new Vector2i();
    }

    default void setSize(Vector2i size) {
    }

    default void setOffset(Vector2i offset) {
    }

    default void modifySubmit(MutablePair<BlockPos, BlockPos> data) {
    }

    interface Pos extends Modifiers<BlockPos> {
        Pos DUMMY = new Dummy();
        class Dummy implements Pos {
            @Override
            public BlockPos modify(BlockPos pos) {
                return pos;
            }

            @Override
            public void onPlace(BlockPos placeAt, Direction direction) {
            }

            @Override
            public void setAnchor(BlockPos pos) {
            }

            @Override
            public BlockPos anchor() {
                return null;
            }

            @Override
            public int priority() {
                return 0;
            }
        }
    }

    interface Dir extends Modifiers<Direction> {
        Dir DUMMY = new Dummy();
        class Dummy implements Dir {
            @Override
            public Direction modify(Direction direction) {
                return direction;
            }

            @Override
            public void onPlace(BlockPos placeAt, Direction direction) {
            }

            @Override
            public void setAnchor(BlockPos direction) {
            }

            @Override
            public BlockPos anchor() {
                return null;
            }

            @Override
            public int priority() {
                return 0;
            }
        }
    }
}
