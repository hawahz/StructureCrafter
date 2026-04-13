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
     * 渲染结构的入口方法，负责协调缓存更新、坐标变换和实际渲染。当结构切换的时候，会调用rebuildCache清空并
     * 更新顶点缓存，且会重置BE缓存，每次渲染都会更新虚拟Level当中的offset和rotation，并接下来依次渲染顶点
     * 缓存的内容和 BE
     *
     * @param poseStack 矩阵栈，用于管理渲染时的坐标变换
     * @param buffer 多重缓冲区，接收不同类型的顶点数据
     * @param camera 相机在世界中的位置坐标
     * @param template 要渲染的结构模板数据
     * @param anchorPos 结构当前的锚点位置（世界坐标）
     * @param oAnchorPos 结构上一帧的锚点位置（用于插值动画）
     * @param center 结构的旋转中心点
     * @param playerDirection 玩家当前的朝向
     * @param oPlayerDirection 玩家上一帧的朝向（用于旋转插值）
     * @param level 当前游戏世界实例
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

    /**
     * 从缓存表当中根据type提取缓存渲染器并传入VertexBuffer顶点缓存进行渲染
     *
     * @param poseStack 矩阵栈，用于管理渲染时的坐标变换
     * @param buffer 多重缓冲区，接收不同类型的顶点数据
     */
    public void renderBuffer(PoseStack poseStack, MultiBufferSource buffer) {
//        Tesselator.getInstance().begin()
        cachedRenderers.forEach(
                (type, renderer) ->
                        renderer.render(poseStack, buffer.getBuffer(type), blockGetter)
        );
    }

    /**
     * 渲染结构中的BlockEntity
     *
     * @param poseStack 矩阵栈，用于管理渲染时的坐标变换
     * @param buffer 多重缓冲区，接收不同类型的顶点数据
     * @param template 结构模板数据
     */
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

    /**
     * 重建顶点缓存，根据区块的所有渲染项去逐个调用原版渲染方法，去将顶点缓存到对应的缓存渲染器当中
     */
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

    /**
     * 渲染项去调用原版渲染方法，begin开始缓存，end结束缓存并将缓存的数据用渲染器包装起来，将缓存的顶点数据传入缓存渲染器中
     *
     * @param renderType 渲染类型
     * @return 缓存渲染器
     */
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

    /**
     * 应用结构偏移，旋转，缩放，平移，最终能够让旋转的中心为结构中心，偏移的结构中心也为结构中心
     * 能够很好地满足其他渲染的预变换，因为其他渲染都是以结构中心为坐标零点
     *
     * @param poseStack 矩阵栈，用于管理渲染时的坐标变换
     * @param camera 相机位置
     * @param anchorPos 结构锚点
     * @param oAnchorPos 上一次结构锚点
     * @param center 结构中心
     * @param playerDirection 玩家朝向
     * @param oPlayerDirection 上一次玩家朝向
     * @param partialTicks 帧数
     */
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