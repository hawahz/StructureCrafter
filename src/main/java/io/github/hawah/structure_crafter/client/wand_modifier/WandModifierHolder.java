package io.github.hawah.structure_crafter.client.wand_modifier;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import io.github.hawah.structure_crafter.client.render.ruler.RulerMaker;
import io.github.hawah.structure_crafter.item.RulerItem;
import io.github.hawah.structure_crafter.util.MutablePair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WandModifierHolder {

    @RulerItem.RulerSetting protected int setting = -1;
    protected final Vector2i size = new Vector2i(), offset = new Vector2i();

    static class ModifierHolder {
        List<Either<Modifiers.Pos, Modifiers.Dir>> modifiers = new ArrayList<>();
        public void push(Modifiers.Pos modifier) {
            modifiers.add(Either.left(modifier));
        }

        public void set(int index, Modifiers.Pos modifier) {
            while (modifiers.size() < index) {
                push(Modifiers.Pos.DUMMY);
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
                push(Modifiers.Dir.DUMMY);
            }
            if (modifiers.size() == index) {
                push(modifier);
            } else {
                modifiers.set(index, Either.right(modifier));
            }
        }

        public Optional<Either<Modifiers.Pos, Modifiers.Dir>> get(int index) {
            if (modifiers.size() <= index)
                return Optional.of(Either.left(Modifiers.Pos.DUMMY));
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
            return modifiers.isEmpty() ||
                    modifiers.stream().allMatch((modifier) ->
                            modifier.left().orElse(Modifiers.Pos.DUMMY).equals(Modifiers.Pos.DUMMY) &&
                                    modifier.right().orElse(Modifiers.Dir.DUMMY).equals(Modifiers.Dir.DUMMY));
        }

        public void reset() {
            modifiers.forEach(modifier -> modifier.ifLeft(m->m.setAnchor(null)).ifRight(m->m.setAnchor(null)));
        }

        public void setAnchor(BlockPos anchor) {
            modifiers.forEach(modifier -> modifier.ifLeft(m->m.setAnchor(anchor)).ifRight(m->m.setAnchor(anchor)));
        }

        public void setSize(Vector2i size) {
            modifiers.forEach(modifier -> modifier.ifLeft(m->m.setSize(size)).ifRight(m->m.setSize(size)));
        }

        public void setOffset(Vector2i offset) {
            modifiers.forEach(modifier -> modifier.ifLeft(m->m.setOffset(offset)).ifRight(m->m.setOffset(offset)));
        }

        public void modifySubmit(MutablePair<BlockPos, BlockPos> data) {
            modifiers.forEach(modifier -> modifier.ifLeft(m->m.modifySubmit(data)).ifRight(m->m.modifySubmit(data)));
        }
    }

    private final ModifierHolder anchorModifiers = new ModifierHolder();
    private final ModifierHolder previewModifiers = new ModifierHolder();
    protected BlockPos anchor, currentPos;

    public WandModifierHolder() {
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
        return currentPos = previewModifiers.modify(MutablePair.of(pos, direction)).left();
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
        if (RulerItem.settingRemained(itemStack, setting) && setting >= 0 && !anchorModifiers.isEmpty() && !previewModifiers.isEmpty()) {
            return;
        }
        int setting = RulerItem.settingOf(itemStack);

        int distance = RulerItem.DISTANCE_MASK & setting;
        if (((setting ^ this.setting) & RulerItem.CHANGE_CENTER) != 0 || anchorModifiers.isEmpty()) {
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
        if (isDistanceChanged) {
            if (distance != 0) {
                previewModifiers.set(1, new FixedDistanceModifier(distance));
            } else {
                previewModifiers.set(1, Modifiers.Pos.DUMMY);
            }
        }
        if (isShapeChanged) {
            if ((setting & RulerItem.IS_CIRCLE) != 0) {
                previewModifiers.set(0, Modifiers.Pos.DUMMY);
            } else {
                previewModifiers.set(0, new StraitModifier());
            }
        }


        //noinspection MagicConstant
        this.setting = setting;
    }

    public void updateStructure(Vector2i size, Vector2i offset) {
        previewModifiers.setSize(size);
        previewModifiers.setOffset(offset);
        this.size.set(size);
        this.offset.set(offset);
    }

    public boolean isEmpty() {
        return anchorModifiers.isEmpty() && previewModifiers.isEmpty() || anchor == null;
    }

    public void submit() {
        if (currentPos == null || anchor == null) {
            RulerMaker.getInstance().chase(this).discard().finish();
            return;
        }
        MutablePair<BlockPos, BlockPos> warpedData = MutablePair.of(anchor, currentPos);
        previewModifiers.modifySubmit(warpedData);
        RulerMaker.getInstance().chase(this, warpedData.left(), warpedData.right())
                .finish();
    }

    public void reset() {
        anchorModifiers.reset();
        previewModifiers.reset();
        anchor = null;
    }
}
