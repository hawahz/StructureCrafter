package io.github.hawah.structure_crafter.client.render;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.OptionalDouble;

import static com.mojang.blaze3d.platform.DepthTestFunction.NO_DEPTH_TEST;
import static net.minecraft.client.renderer.rendertype.LayeringTransform.*;
import static net.minecraft.client.renderer.rendertype.OutputTarget.*;

public class OverRenderType {
    // 这是一个 dummy 构造器，只是为了能继承 RenderType 访问 protected 方法


    // 定义透视线框 RenderType
    public static final RenderType OVERLAY_LINES = RenderType.create(
            "overlay_lines",
            RenderSetup.builder(RenderPipelines.LINES).createRenderSetup()
    );
//
//    public static final RenderType OVERLAY_LINES = create(
//            "overlay_lines",
//            DefaultVertexFormat.POSITION_COLOR_NORMAL,
//            VertexFormat.Mode.LINES,
//            256,
//            false,
//            false,
//            RenderType.CompositeState.builder()
//                    .setShaderState(RENDERTYPE_LINES_SHADER)
//                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
//                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
//                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
//                    .setOutputState(ITEM_ENTITY_TARGET)
//                    .setWriteMaskState(COLOR_DEPTH_WRITE)
//                    .setCullState(NO_CULL)
//                    .setDepthTestState(NO_DEPTH_TEST)
//                    .createCompositeState(false)
//    );
}
