package io.github.hawah.structure_crafter.client.render.structure;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.hawah.structure_crafter.client.handler.StructureWandHandler;
import io.github.hawah.structure_crafter.mixin.StructureTemplateAccessor;
import io.github.hawah.structure_crafter.client.utils.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.HashMap;
import java.util.Map;

public class StructureRenderer {

    // 缓存生成的 BlockEntity，避免每帧重复解析 NBT 导致严重掉帧
    private final Map<BlockPos, BlockEntity> cachedBlockEntities = new HashMap<>();
    private final Map<RenderType, WarpedBufferRenderer> cachedRenderers = new HashMap<>();
    private StructureBlockGetter blockGetter;
    private boolean dirty = false;

    /**
     * 主渲染方法
     *
     * @param poseStack 矩阵栈
     * @param buffer    渲染缓冲区 (SuperRenderTypeBuffer 或 MultiBufferSource)
     * @param camera    当前相机坐标
     * @param template  要渲染的结构模板
     * @param anchorPos 结构在世界中实际放置的起始坐标
     * @param level     当前世界
     */
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            Vec3 camera,
            StructureTemplate template,
            BlockPos anchorPos,
            BlockPos oAnchorPos,
            BlockPos center,
            Direction playerDirection,
            Direction oPlayerDirection,
            Level level
    ) {
        if (((StructureTemplateAccessor) template).getPalettes().isEmpty()) return;

        if (dirty) {
            blockGetter = new StructureBlockGetter(template, level);
            rebuildCache();
            clearCache();
            dirty = false;
        }

        blockGetter.setOffset(anchorPos);
        blockGetter.setRot(StructureWandHandler.transferDirectionToRotation(playerDirection));

        float partialTicks = AnimationTickHolder.getPartialTicks();

        poseStack.pushPose();
        applyTransform(poseStack, camera, anchorPos, oAnchorPos, center, playerDirection, oPlayerDirection, partialTicks);
        renderBuffer(poseStack, buffer);
        renderBlockEntities(poseStack, buffer, (StructureTemplateAccessor) template);
        poseStack.popPose();
    }

    public void renderBuffer(PoseStack poseStack, MultiBufferSource buffer) {
//        Tesselator.getInstance().begin()
        cachedRenderers.forEach(
                (type, renderer) ->
                        renderer.render(poseStack, buffer.getBuffer(type), blockGetter)
        );
    }

    private void renderBlockEntities(PoseStack poseStack, MultiBufferSource buffer, StructureTemplateAccessor template) {
        Minecraft mc = Minecraft.getInstance();
        BlockEntityRenderDispatcher beDispatcher = mc.getBlockEntityRenderDispatcher();
        var blockInfos = template.getPalettes().getFirst().blocks();

        for (StructureTemplate.StructureBlockInfo info : blockInfos) {
            if (info.nbt() == null) {
                continue;
            }
            BlockState state = info.state();
            if (!state.hasBlockEntity()) {
                continue;
            }

            poseStack.pushPose();
            poseStack.translate(info.pos().getX(), info.pos().getY(), info.pos().getZ());
            BlockEntity be = getOrCreateBlockEntity(info.pos(), state, info.nbt());
            if (be != null) {
                renderBlockEntity(be, poseStack, buffer, beDispatcher, info.pos());
            }
            poseStack.popPose();
        }
    }

    public void rebuildCache() {
        // Init
        cachedRenderers.clear();
        // Build cache
        for (RenderType type: RenderType.chunkBufferLayers()) {
            WarpedBufferRenderer cachedRenderer = buildCache(type);
            if (!cachedRenderer.isEmpty()) {
                cachedRenderers.put(type, cachedRenderer);
            }
        }
    }

    protected WarpedBufferRenderer buildCache(RenderType renderType) {
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        ModelBlockRenderer modelRenderer = blockRenderer.getModelRenderer();

        PoseStack poseStack = new PoseStack();
        RandomSource random = RandomSource.create();
        BoundingBox bounds = blockGetter.getBounds();// template.getBoundingBox(new StructurePlaceSettings(), anchorPos);

        WarpedBufferBuilder builder = new WarpedBufferBuilder();
        builder.begin();

        for (BlockPos localPos : BlockPos.betweenClosed(bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(), bounds.maxY(), bounds.maxZ())) {
            BlockState blockState = blockGetter.getBlockState(localPos);
            if (!RenderShape.MODEL.equals(blockState.getRenderShape())) {
                continue;
            }

            BakedModel bakedModel = blockRenderer.getBlockModel(blockState);
            ModelData modelData = bakedModel.getModelData(blockGetter, localPos, blockState, ModelData.EMPTY);
            long seed = blockState.getSeed(localPos);
            random.setSeed(seed);

            if (!bakedModel.getRenderTypes(blockState, random, modelData).contains(renderType))
                continue;

            poseStack.pushPose();
            poseStack.translate(localPos.getX(), localPos.getY(), localPos.getZ());

            modelRenderer.tesselateBlock(
                    blockGetter,
                    bakedModel,
                    blockState,
                    localPos,
                    poseStack,
                    builder,
                    true,
                    random,
                    seed,
                    OverlayTexture.NO_OVERLAY,
                    modelData,
                    renderType
            );
            poseStack.popPose();
        }
        ModelBlockRenderer.clearCache();

        return builder.end();
    }


    private static void applyTransform(PoseStack poseStack,
                                       Vec3 camera,
                                       BlockPos anchorPos,
                                       BlockPos oAnchorPos,
                                       BlockPos center,
                                       Direction playerDirection,
                                       Direction oPlayerDirection,
                                       float partialTicks) {
        Vec3 offset = new Vec3(
                Mth.lerp(partialTicks, oAnchorPos.getX(), anchorPos.getX()),
                Mth.lerp(partialTicks, oAnchorPos.getY(), anchorPos.getY()),
                Mth.lerp(partialTicks, oAnchorPos.getZ(), anchorPos.getZ())
        );
        poseStack.translate(
                - camera.x(),
                - camera.y(),
                - camera.z()
        );

        float degree = getDegree(playerDirection, oPlayerDirection, partialTicks);
        poseStack.translate(
                offset.x() + 0.5,
                offset.y(),
                offset.z() + 0.5
        );
        poseStack.mulPose(Axis.YN.rotationDegrees(degree));
        poseStack.translate(
                -center.getX()- 0.5,
                 0,
                -center.getZ()- 0.5
        );
    }

    private void renderBlockEntity(BlockEntity blockEntity,
                                   PoseStack ms,
                                   MultiBufferSource buffer,
                                   BlockEntityRenderDispatcher dispatcher,
                                   BlockPos localPos) {
        BlockEntityRenderer<BlockEntity> renderer = dispatcher.getRenderer(blockEntity);
        if (renderer != null) {
            float partialTick = AnimationTickHolder.getPartialTicks();

            // 组合光照值
            int packedLight = LevelRenderer.getLightColor(blockGetter, localPos);
            int packedOverlay = net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;

            // 调用渲染器
            renderer.render(blockEntity, partialTick, ms, buffer, packedLight, packedOverlay);
        }
    }

    public void setDirty() {
        dirty = true;
    }

    private static float getDegree(Direction playerDirection, Direction oPlayerDirection, float partialTicks) {
        int rotateAngle = 0;
        int oRotateAngle = 0;
        switch (playerDirection) {
            case EAST -> rotateAngle = 90;
            case SOUTH -> rotateAngle = 180;
            case WEST -> rotateAngle = 270;
            default -> {}
        }

        switch (oPlayerDirection) {
            case EAST -> oRotateAngle = 90;
            case SOUTH -> oRotateAngle = 180;
            case WEST -> oRotateAngle = 270;
            default -> {}
        }

        return Mth.rotLerp(
                partialTicks,
                oRotateAngle,
                rotateAngle
        );
    }

    private BlockEntity getOrCreateBlockEntity(BlockPos localPos, BlockState state, CompoundTag nbt) {
        // 使用世界绝对坐标作为缓存键 (或者使用 LocalPos，取决于你的结构是否会移动)
        return cachedBlockEntities.computeIfAbsent(localPos, pos -> {
            // 复制一份 NBT，并强制修改坐标，防止与现存实体的坐标冲突
            CompoundTag modifiedNbt = nbt.copy();
            modifiedNbt.putInt("x", pos.getX());
            modifiedNbt.putInt("y", pos.getY());
            modifiedNbt.putInt("z", pos.getZ());

            // 从 NBT 加载一个游离的 BlockEntity
            BlockEntity blockEntity;
            if (state.getBlock() instanceof EntityBlock block) {
                    blockEntity = block.newBlockEntity(pos, state);
            } else {
                blockEntity = BlockEntity.loadStatic(pos, state, modifiedNbt, blockGetter.registryAccess());
            }
            if (blockEntity != null) {
                blockEntity.setLevel(blockGetter);
            }
            return blockEntity;
        });
    }

    // 如果你的结构发生变化或被销毁，记得调用此方法清理内存
    public void clearCache() {
        cachedBlockEntities.clear();
    }
}