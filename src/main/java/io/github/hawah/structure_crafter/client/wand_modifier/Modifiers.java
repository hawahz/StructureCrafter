package io.github.hawah.structure_crafter.client.wand_modifier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public interface Modifiers<T> {
    T modify(T t);
    void onPlace(BlockPos placeAt, Direction direction);
    void setAnchor(BlockPos t);
    BlockPos anchor();
    int priority();

    interface Pos extends Modifiers<BlockPos> {
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
