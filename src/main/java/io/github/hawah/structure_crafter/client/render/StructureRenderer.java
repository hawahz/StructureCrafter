package io.github.hawah.structure_crafter.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.hawah.structure_crafter.client.handler.StructureWandHandler;
import io.github.hawah.structure_crafter.mixin.StructureTemplateAccessor;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public class StructureRenderer {

    // 缓存生成的 BlockEntity，避免每帧重复解析 NBT 导致严重掉帧
    private final Map<BlockPos, BlockEntity> cachedBlockEntities = new HashMap<>();
    private final Map<BlockState, ModelData> cachedModelData = new HashMap<>();
    private StructureTemplate template;
    private StructureBlockGetter blockGetter;
    private AABB boundingBox = null;

    /**
     * 主渲染方法
     * @param poseStack 矩阵栈
     * @param buffer 渲染缓冲区 (SuperRenderTypeBuffer 或 MultiBufferSource)
     * @param camera 当前相机坐标
     * @param template 要渲染的结构模板
     * @param anchorPos 结构在世界中实际放置的起始坐标
     * @param level 当前世界
     */
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            Vec3 camera,
            StructureTemplate template,
            BlockPos anchorPos,
            BlockPos oAnchorPos,
            Direction playerDirection,
            Direction oPlayerDirection,
            Level level
    ) {
        if (((StructureTemplateAccessor) template).getPalettes().isEmpty()) return;

        if (this.template != template) {
            this.template = template;
            blockGetter = new StructureBlockGetter(template, anchorPos, level);
            clearCache();
        }

        boundingBox = AABB.of(template.getBoundingBox(
                new StructurePlaceSettings()
                        .setRotation(StructureWandHandler.transferDirectionToRotation(playerDirection)),
                anchorPos
        ));
        Frustum frustum = Minecraft.getInstance().levelRenderer.getFrustum();
        if (!frustum.isVisible(boundingBox)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        BlockEntityRenderDispatcher beDispatcher = mc.getBlockEntityRenderDispatcher();
        float partialTicks = AnimationTickHolder.getPartialTicks();

        // 1. 准备全局位移：将渲染起点移动到结构的世界原点
        poseStack.pushPose();
        poseStack.translate(
                Mth.lerp(partialTicks, oAnchorPos.getX(), anchorPos.getX()) - camera.x(),
                Mth.lerp(partialTicks, oAnchorPos.getY(), anchorPos.getY()) - camera.y(),
                Mth.lerp(partialTicks, oAnchorPos.getZ(), anchorPos.getZ()) - camera.z()
        );

        // 获取结构的方块列表（假设使用第一个调色板）
        var blockInfos = ((StructureTemplateAccessor) template).getPalettes().getFirst().blocks();

        float degree = getDegree(playerDirection, oPlayerDirection, partialTicks);
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YN.rotationDegrees(degree));
        poseStack.translate(-0.5, -0.5, -0.5);

        // 2. 遍历渲染所有的方块和方块实体
        for (StructureTemplate.StructureBlockInfo info : blockInfos) {
            BlockState state = info.state();
            if (state.isAir()) continue;


            BlockPos localPos = info.pos();
            BlockPos worldPos = anchorPos.offset(localPos); // 方块在世界中的真实坐标

            BlockPos posLight = anchorPos.offset(localPos.rotate(Rotation.values()[Math.floorMod((int) (degree / 90), 4)]));
            if (!frustum.isVisible(new AABB(posLight))) {
                continue;
            }

            poseStack.pushPose();

            poseStack.translate(localPos.getX(), localPos.getY(), localPos.getZ());


            // --- A. 渲染静态方块模型 ---
            if (state.getRenderShape() == RenderShape.MODEL) {
                renderStaticBlock(
                        poseStack,
                        buffer,
                        mc,
                        blockGetter,
                        state,
                        worldPos,
                        level.getRandom()
                );
            }

            // --- B. 渲染方块实体 (BER / 动态渲染) ---
            if (info.nbt() != null && state.hasBlockEntity()) {
                // 如果是特殊渲染形状 (例如箱子、床) 或附加了 BER 的普通方块
                poseStack.pushPose();
                BlockEntity be = getOrCreateBlockEntity(level, info.pos(), worldPos, state, info.nbt());
                if (be != null) {
                    renderBlockEntity(be, poseStack, buffer, beDispatcher, level, worldPos);
                }
                poseStack.popPose();
            }

            poseStack.popPose();
        }

        poseStack.popPose();
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

        float degree = Mth.rotLerp(
                partialTicks,
                oRotateAngle,
                rotateAngle
        );
        return degree;
    }

    private void renderStaticBlock(PoseStack ms, MultiBufferSource buffer, Minecraft mc, BlockAndTintGetter level, BlockState state, BlockPos worldPos, RandomSource randomSource) {
        // 模型数据缓存
        ModelData modelData;
        if (cachedModelData.containsKey(state))
            modelData = cachedModelData.get(state);
        else {
            modelData = level.getModelData(worldPos);
            cachedModelData.put(state, modelData);
        }
        if (modelData == null) modelData = ModelData.EMPTY;

        BakedModel bakedModel = mc.getBlockRenderer().getBlockModel(state);

        var renderTypes = bakedModel.getRenderTypes(state, randomSource, modelData);

        for (RenderType type : renderTypes) {
            VertexConsumer consumer = buffer.getBuffer(type);
            mc.getBlockRenderer().renderBatched(
                    state,
                    worldPos,
                    blockGetter,
                    ms,
                    consumer,
                    true, // 剔除遮挡面
                    randomSource,
                    modelData,
                    type
            );
        }
    }

    private void renderBlockEntity(BlockEntity blockEntity,
                                   PoseStack ms,
                                   MultiBufferSource buffer,
                                   BlockEntityRenderDispatcher dispatcher,
                                   Level level,
                                   BlockPos worldPos) {
        // 确保 BER 存在且应该被渲染
        BlockEntityRenderer<BlockEntity> renderer = dispatcher.getRenderer(blockEntity);
        if (renderer != null) {
            float partialTick = AnimationTickHolder.getPartialTicks();

            // 组合光照值
            int packedLight = LevelRenderer.getLightColor(level, worldPos);
            int packedOverlay = net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;

            // 调用渲染器
            renderer.render(blockEntity, partialTick, ms, buffer, packedLight, packedOverlay);
        }
    }

    private BlockEntity getOrCreateBlockEntity(Level level, BlockPos localPos, BlockPos worldPos, BlockState state, CompoundTag nbt) {
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
                blockEntity = BlockEntity.loadStatic(pos, state, modifiedNbt, level.registryAccess());
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
        cachedModelData.clear();
    }
}