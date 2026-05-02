package io.github.hawah.structure_crafter.client.wand_modifier;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import io.github.hawah.structure_crafter.item.RulerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WandModifierHolder {

    @RulerItem.RulerSetting protected int setting = -1;

    static class ModifierHolder {
        List<Either<Modifiers.Pos, Modifiers.Dir>> modifiers = new ArrayList<>();
        public void push(Modifiers.Pos modifier) {
            modifiers.add(Either.left(modifier));
        }

        public void set(int index, Modifiers.Pos modifier) {
            while (modifiers.size() < index) {
                push(new Modifiers.Pos.Dummy());
            }
            if (modifiers.size() == index) {
                push(modifier);
            } else {
                modifiers.set(index, Either.left(modifier));
            }
        }

        public void push(Modifiers.Dir modifier) {
            modifiers.add(Either.right(modifier));
        }

        public void set(int index, Modifiers.Dir modifier) {
            while (modifiers.size() < index) {
                push(new Modifiers.Dir.Dummy());
            }
            if (modifiers.size() == index) {
                push(modifier);
            } else {
                modifiers.set(index, Either.right(modifier));
            }
        }

        public Optional<Either<Modifiers.Pos, Modifiers.Dir>> get(int index) {
            if (modifiers.size() <= index)
                return Optional.of(Either.left(new Modifiers.Pos.Dummy()));
            return Optional.of(modifiers.get(index));
        }

        public MutablePair<BlockPos, Direction> modify(MutablePair<BlockPos, Direction> data) {
            modifiers.forEach(
                    modifier ->
                            modifier
                                    .ifLeft(mod -> data.setLeft(mod.modify(data.left())))
                                    .ifRight(mod -> data.setRight(mod.modify(data.right())))
              );

            return data;
        }

        public void onPlace(BlockPos placeAt, Direction direction) {
            for (Either<Modifiers.Pos, Modifiers.Dir> modifier : modifiers) {
                modifier.ifLeft(mod -> mod.onPlace(placeAt, direction));
                modifier.ifRight(mod -> mod.onPlace(placeAt, direction));
            }
        }

        public void clear() {
            modifiers.clear();
        }

        public boolean isEmpty() {
            return modifiers.isEmpty();
        }

        public void reset() {
            modifiers.forEach(modifier -> modifier.ifLeft(m->m.setAnchor(null)).ifRight(m->m.setAnchor(null)));
        }

        public void setAnchor(BlockPos anchor) {
            modifiers.forEach(modifier -> modifier.ifLeft(m->m.setAnchor(anchor)).ifRight(m->m.setAnchor(anchor)));
        }
    }

    static class MutablePair<L, R> {
        public L left;
        public R right;
        public MutablePair(L left, R right) {
            this.left = left;
            this.right = right;
        }
        public MutablePair<R, L> swap() {
            return new MutablePair<>(right, left);
        }
        public L left() {
            return left;
        }
        public R right() {
            return right;
        }
        public void setLeft(L left) {
            this.left = left;
        }
        public void setRight(R right) {
            this.right = right;
        }

        @SuppressWarnings("unchecked")
        public <T> void set(T value) {
            if (left.getClass().equals(value.getClass())) {
                setLeft((L) value);
            } else if (right.getClass().equals(value.getClass())) {
                setRight((R) value);
            }
        }

        public static <U, V> MutablePair<U, V> of(U left, V right) {
            return new MutablePair<>(left, right);
        }
    }

    private final ModifierHolder anchorModifiers = new ModifierHolder();
    private final ModifierHolder previewModifiers = new ModifierHolder();
    protected BlockPos anchor;

    public WandModifierHolder() {
        anchorModifiers.push(new Modifiers.Pos.Dummy());
        previewModifiers.push(new Modifiers.Pos.Dummy());
        previewModifiers.push(new Modifiers.Pos.Dummy());
    }

    public Direction applyDirectionModifier(BlockPos pos, Direction direction) {
        if (previewModifiers.isEmpty() || pos == null || direction == null) {
            return direction;
        }
        return previewModifiers.modify(MutablePair.of(pos, direction)).right();
    }

    public BlockPos applyPosModifier(BlockPos pos, Direction direction) {
        if (previewModifiers.isEmpty() || pos == null || direction == null) {
            return pos;
        }
        return previewModifiers.modify(MutablePair.of(pos, direction)).left();
    }

    public void onPlace(BlockPos placeAt, Direction direction) {
        anchorModifiers.onPlace(placeAt, direction);
        try {
            anchor = anchorModifiers.modifiers.getLast().left().orElseThrow().anchor();
            for (Either<Modifiers.Pos, Modifiers.Dir> modifier : previewModifiers.modifiers) {
                Optional<Modifiers.Pos> left = modifier.left();
                left.ifPresent(pos -> pos.setAnchor(anchor));
            }
        } catch (Exception e) {
            LogUtils.getLogger().error("Error when modify anchor.", e);
        }
        previewModifiers.onPlace(placeAt, direction);
    }

    public void update(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            reset();
            return;
        }
        if (RulerItem.settingRemained(itemStack, setting) && setting >= 0) {
            return;
        }
        int setting = RulerItem.settingOf(itemStack);

        int distance;
        if (((setting ^ this.setting) & RulerItem.CHANGE_CENTER) != 0) {
            anchorModifiers.clear();
            if ((setting & RulerItem.CHANGE_CENTER) != 0) {
                anchorModifiers.push(new ChainedAnchorModifier());
            } else {
                anchorModifiers.push(new FixedAnchorModifier());
            }
        }

        boolean isShapeChanged = ((setting ^ this.setting) & RulerItem.IS_CIRCLE) != 0;
        boolean isDistanceChanged = ((setting ^ this.setting) & RulerItem.DISTANCE_MASK) != 0;

        if (!isShapeChanged && !isDistanceChanged) {
            //noinspection MagicConstant
            this.setting = setting;
            return;
        }
        if (isShapeChanged) {
            if ((setting & RulerItem.IS_CIRCLE) != 0) {
                previewModifiers.set(0, new Modifiers.Pos.Dummy());
            } else {
                previewModifiers.set(0, new StraitModifier());
            }
        }
        if (isDistanceChanged) {
            if ((distance = RulerItem.DISTANCE_MASK & setting) != 0) {
                previewModifiers.set(1, new FixedDistanceModifier(distance));
            } else {
                previewModifiers.set(1, new Modifiers.Pos.Dummy());
            }
        }

        //noinspection MagicConstant
        this.setting = setting;
    }

    public void submit() {

    }

    public void reset() {
        anchorModifiers.reset();
        previewModifiers.reset();
        anchor = null;
    }
}
