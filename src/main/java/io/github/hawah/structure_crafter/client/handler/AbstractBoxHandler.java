package io.github.hawah.structure_crafter.client.handler;

import io.github.hawah.structure_crafter.Config;
import io.github.hawah.structure_crafter.client.render.outliner.Outliner;
import io.github.hawah.structure_crafter.compat.sable.SableLogicTransformCompat;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import io.github.hawah.structure_crafter.util.KeyBinding;
import io.github.hawah.structure_crafter.util.RaycastHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class AbstractBoxHandler implements IHandler{
    protected Object outlineSlot = new Object();
    protected BlockPos firstPos;
    protected BlockPos secondPos;
    protected BlockPos selectedPos;
    protected AABB cachedBoundingBox;
    protected Direction selectedFace;
    protected int reach = 4;
    protected int scrolling = 0;
    @SuppressWarnings("FieldCanBeLocal")
    protected final int MAX_REACH = 100;

    public AbstractBoxHandler() {
        KeyBinding.RIGHT.bind(KeyBinding.Action.of(
                ()-> this.isActive() && selectedPos != null,
                () -> {
                    if (firstPos == null) {
                        firstPos = selectedPos;
                        return;
                    }
                    if (secondPos == null) {
                        setSecondPos(selectedPos);
                        return;
                    }
                    firstPos = selectedPos;
                    setSecondPos(null);
                },
                () -> selectedPos == null?
                        firstPos == null?
                                LangData.HUD_TIP_BLACKBOARD_SELECT_FIRST_POINT.get():
                                LangData.HUD_TIP_BLACKBOARD_SELECT_SECOND_POINT.get():
                        LangData.HUD_TIP_BLACKBOARD_CLEAR_AND_SELECT_FIRST.get()
        ));
        KeyBinding.SHIFT_R.bind(KeyBinding.Action.of(
                () -> this.isActive() && firstPos != null,
                this::delete,
                LangData.HUD_TIP_BLACKBOARD_DELETE_ALL.get()
        ));
        KeyBinding.CTRL.bind(KeyBinding.Action.of(
                () -> this.isActive() && firstPos != null && secondPos != null,
                KeyBinding.Action.EMPTY,
                LangData.HUD_TIP_BLACKBOARD_SHOW_ALL_FACES.get()
        ));
        KeyBinding.CTRL_L.bind(KeyBinding.Action.of(
                () -> this.isActive() && (firstPos == null || secondPos == null),
                KeyBinding.Action.EMPTY,
                LangData.HUD_TIP_BLACKBOARD_PICK_AIR_CENTER.get()
        ));
        KeyBinding.CTRL_R.bind(KeyBinding.Action.of(
                () -> this.isActive() && (firstPos == null || secondPos == null),
                KeyBinding.Action.EMPTY,
                LangData.HUD_TIP_BLACKBOARD_PICK_AIR_POINT.get()
        ));
        KeyBinding.CTRL_S.bind(KeyBinding.Action.of(
                () -> this.isActive() && (firstPos == null || secondPos == null),
                () -> {
                    int intDelta = KeyBinding.KeyBuffer.getIntDelta();
                    reach = Mth.clamp(reach + intDelta, 0, MAX_REACH);
                },
                LangData.HUD_TIP_BLACKBOARD_CHANGE_DISTANCE.get()
        ));
        // TODO Take over scrolling
        KeyBinding.CTRL_ALT_S.bind(KeyBinding.Action.of(
                () -> this.isActive() && selectedFace != null,
                () -> {
                    int intDelta = KeyBinding.KeyBuffer.getIntDelta();
                    pushOrPullFace(intDelta, true);
                },
                LangData.HUD_TIP_BLACKBOARD_SELECT_OPPOSITE_FACE.get(),
                () -> {
//                    int intDelta = KeyBinding.KeyBuffer.getIntDelta();
//                    pushOrPullFace(intDelta, true);
                }
        ));
        KeyBinding.ALT_S.bind(KeyBinding.Action.of(
                () -> this.isActive() && selectedFace != null,
                () -> {
                    int intDelta = KeyBinding.KeyBuffer.getIntDelta();
                    pushOrPullFace(intDelta, false);
                },
                LangData.HUD_TIP_BLACKBOARD_PUSH_OR_PULL_FACE.get()
        ));
    }

    public void setSelectedPos(BlockPos selectedPos) {
        if (Objects.equals(this.selectedPos, selectedPos)) {
            return;
        }
        this.selectedPos = transform(selectedPos);

        if (secondPos != null) {
            return;
        }

        updateBoundingBox();
    }

    public void setSecondPos(BlockPos secondPos) {
        if (Objects.equals(this.secondPos, secondPos)) {
            return;
        }

        List<BlockPos> resultHolder = Arrays.asList(firstPos, secondPos);

        SableLogicTransformCompat.instance().applyReverseAreaTotalTransform(secondPos, resultHolder);

        this.firstPos = resultHolder.getFirst();
        this.secondPos = resultHolder.getLast();

        updateBoundingBox();
    }

    public void updateBoundingBox() {

        if (firstPos == null || (secondPos == null && selectedPos == null)) {
            return;
        }

        BlockPos first = firstPos;
        BlockPos second = secondPos == null? (selectedPos == null? firstPos: selectedPos) : secondPos;
        cachedBoundingBox = new AABB(
                new Vec3(
                        Math.min(first.getX(), second.getX()),
                        Math.min(first.getY(), second.getY()),
                        Math.min(first.getZ(), second.getZ())
                ),
                new Vec3(
                        Math.max(first.getX(), second.getX()) + 1.0,
                        Math.max(first.getY(), second.getY()) + 1.0,
                        Math.max(first.getZ(), second.getZ()) + 1.0
                )
        );
    }

    @Override
    public void tick() {
        if (!isVisible()) {
            Outliner.getInstance().thickBox(outlineSlot)
                    .fade()
                    .finish();
        }

        if (!isActive()) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        BlockHitResult trace = RaycastHelper.rayTraceRange(player.level(), player, 75);

        // buffered selected pos
        if (trace.getType() == HitResult.Type.BLOCK) {
            setSelectedPos(trace.getBlockPos());
        } else {
            setSelectedPos(null);
        }

        // select face
        if (firstPos != null && secondPos != null && Screen.hasAltDown()) {
            selectedFace = scrolling <= 0? RaycastHelper.intersectRayWithBox(
                    player.getEyePosition(),
                    RaycastHelper.getTraceTarget(player, 300, player.getEyePosition())
            ) : selectedFace;
            if (canSelectOpposite()) {
                selectedFace = selectedFace.getOpposite();
            }
            scrolling = Math.max(0, scrolling - 1);
        } else{
            selectedFace = null;
            scrolling = 0;
        }

        // select in the air
        if (canReachAir()) {
            Vec3 targetVec = transform(player.getEyePosition(0)
                    .add(player.getLookAngle()
                            .scale(reach)));
            setSelectedPos(BlockPos.containing(targetVec));
        }

        if (firstPos != null && (selectedPos != null || secondPos != null) && cachedBoundingBox != null) {
            Vec3i size = (secondPos == null? selectedPos : secondPos).subtract(firstPos);

            int volume = (Math.abs(size.getX()) + 1) * (Math.abs(size.getY()) + 1) * (Math.abs(size.getZ()) + 1);
            player.displayClientMessage(
                    LangData.HUD_BLACKBOARD_SELECTION.get(
                            (isOversizeX()?"§c" : "") + (Math.abs(size.getX()) + 1) + "§r",
                            (isOversizeY()?"§c" : "") + (Math.abs(size.getY()) + 1) + "§r",
                            (isOversizeZ()?"§c" : "") + (Math.abs(size.getZ()) + 1) + "§r",
                            (volume > Config.ServerConfig.MAX_VOLUME.get()? "§c" : "") + volume
                    ),
                    true
            );
        }
    }


    public boolean hasSelection() {
        return firstPos != null;
    }

    /**
     * Discard present handler data and outline data. Usually cause a fade and discard on the current binded outline
     * */
    public void discard() {
        outlineSlot = new Object();
        firstPos = null;
        selectedPos = null;
        secondPos = null;
    }

    public void delete() {
        Outliner.getInstance().thickBox(outlineSlot)
                .setRGBA(1, 0, 0, 1)
                .lazyFade(40)
                .discard()
                .finish();
        discard();

    }

    protected void pushOrPullFace(int intDelta, boolean opposite) {
        Vec3i normal = selectedFace.getNormal();
        AABB box = cachedBoundingBox;
        AABB aabb = (intDelta < 0) ^ opposite?
                box.expandTowards(normal.getX(), normal.getY(), normal.getZ()) :
                box.contract(normal.getX(), normal.getY(), normal.getZ());
        firstPos = BlockPos.containing(aabb.getMinPosition());
        setSecondPos(BlockPos.containing(aabb.getMaxPosition().add(new Vec3(-1, -1, -1))));
        updateBoundingBox();
        scrolling += Math.abs(intDelta * 2);
        scrolling = Math.min(scrolling, 6);
    }

    public boolean isValidSize() {
        return isOversizeX() ||
                isOversizeY() ||
                isOversizeZ();
    }

    protected abstract boolean isOversizeZ();

    protected abstract boolean isOversizeY();

    protected abstract boolean isOversizeX();

    protected boolean isPhysicalSide() {
        return SableLogicTransformCompat.instance().isPhysical(firstPos);
    }

    protected BlockPos transform(BlockPos pos) {
        SableLogicTransformCompat transformer = SableLogicTransformCompat.instance();
        if (isPhysicalSide()) {
            return transformer.isSameSide(pos, firstPos)? pos: transformer.applyTransformInverse(pos, firstPos);
        } else {
            return transformer.isSameSide(pos, firstPos)? pos: transformer.applyTransform(pos);
        }
    }

    protected Vec3 transform(Vec3 pos) {
        SableLogicTransformCompat transformer = SableLogicTransformCompat.instance();
        if (isPhysicalSide()) {
            return transformer.isPhysical(pos)? pos: transformer.applyTransformInverse(pos, firstPos.getCenter());
        } else {
            return transformer.isPhysical(pos)? transformer.applyTransform(pos, firstPos.getCenter()): pos;
        }
    }

    protected boolean canPushOrPullFace() {
        return selectedFace != null && Screen.hasAltDown();
    }

    protected boolean canReachAir() {
        return Screen.hasControlDown() && (firstPos == null || secondPos == null);
    }

    protected boolean canSelectOpposite() {
        return Screen.hasControlDown() && selectedFace != null && scrolling <= 0;
    }
}
