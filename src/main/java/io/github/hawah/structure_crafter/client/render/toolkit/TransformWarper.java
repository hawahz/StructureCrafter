package io.github.hawah.structure_crafter.client.render.toolkit;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber
public class TransformWarper {
    private static final Map<Object, TransformWarper> INSTANCES = new HashMap<>();

    public static TransformWarper instance(Object object) {
        if (!INSTANCES.containsKey(object)) {
            INSTANCES.put(object, new TransformWarper());
        }
        return INSTANCES.get(object);
    }

    private Matrix4f poseMatrix = new Matrix4f();
    private PoseStack poseStack = new PoseStack();
    private boolean active = false;
    public TransformWarper warp(PoseStack poseStack) {
        poseStack.pushPose();
        this.poseStack = poseStack;
        this.apply(poseStack);
        return this;
    }

    public TransformWarper end() {
        try {
            poseStack.popPose();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            poseStack = null;
        }
        return this;
    }
    public PoseStack apply(PoseStack poseStack) {
        poseStack.mulPose(poseMatrix);
        return poseStack;
    }

    public void setPoseMatrix(Matrix4f matrix4f) {
        poseMatrix.set(matrix4f);
    }
    public static boolean onKeyPressed(int button, boolean pressed) {
        return INSTANCES.values().stream()
                .map(transformWarper -> transformWarper.handleKeyPressed(button, pressed))
                .anyMatch(b -> b);
    }

    private boolean handleKeyPressed(int button, boolean pressed) {
        if (!pressed)
            return false;
        if (button == GLFW.GLFW_KEY_ENTER) {
            this.active = !this.active;
            if (!this.active) {
                printMatrix();
                copyMatrix();
            }
        }
        if (!this.active) {
            return false;
        }
        switch (button) {
            case GLFW.GLFW_KEY_LEFT -> {
                poseMatrix.translate(-0.1F, 0, 0);
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                poseMatrix.translate(0.1F, 0, 0);
            }
            case GLFW.GLFW_KEY_UP -> {
                poseMatrix.translate(0, 0.1F, 0);
            }
            case GLFW.GLFW_KEY_DOWN -> {
                poseMatrix.translate(0, -0.1F, 0);
            }
            case GLFW.GLFW_KEY_Z -> {
                poseMatrix.translate(0, 0, 0.1F);
            }
            case GLFW.GLFW_KEY_X -> {
                poseMatrix.translate(0, 0, -0.1F);
            }
            case GLFW.GLFW_KEY_EQUAL -> {
                poseMatrix.scale(1.1F, 1.1F, 1.1F);
            }
            case GLFW.GLFW_KEY_MINUS -> {
                poseMatrix.scale(0.9F, 0.9F, 0.9F);
            }
            case GLFW.GLFW_KEY_U -> {
                poseMatrix.rotate(0.1F, 0, 0, 1);
            }
            case GLFW.GLFW_KEY_O -> {
                poseMatrix.rotate(-0.1F, 0, 0, 1);
            }
            case GLFW.GLFW_KEY_J -> {
                poseMatrix.rotate(-0.1F, 0, 1, 0);
            }
            case GLFW.GLFW_KEY_L -> {
                poseMatrix.rotate(0.1F, 0, 1, 0);
            }
            case GLFW.GLFW_KEY_I -> {
                poseMatrix.rotate(-0.1F, 1, 0, 0);
            }
            case GLFW.GLFW_KEY_K -> {
                poseMatrix.rotate(0.1F, 1, 0, 0);
            }
            case GLFW.GLFW_KEY_R -> {
                poseMatrix.set(new Matrix4f());
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    public void render() {

    }

    public void copyMatrix() {
        copyMatrix(this.poseMatrix);
    }

    public static void copyMatrix(Matrix4f matrix4f) {
        try {
            if (!GraphicsEnvironment.isHeadless()) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(matrixToString(matrix4f)), null);
            }
        } catch (HeadlessException ignored) {
        }
    }
    public void printMatrix() {
        System.out.println(
                matrixToString()
        );
    }

    public String matrixToString() {
        return matrixToString(this.poseMatrix);
    }

    public static String matrixToString(Matrix4f matrix4f) {
        return "new Matrix4f(" +
                matrix4f.m00() + "f, " + matrix4f.m01() + "f, " + matrix4f.m02() + "f, " + matrix4f.m03() + "f, " +
                matrix4f.m10() + "f, " + matrix4f.m11() + "f, " + matrix4f.m12() + "f, " + matrix4f.m13() + "f, " +
                matrix4f.m20() + "f, " + matrix4f.m21() + "f, " + matrix4f.m22() + "f, " + matrix4f.m23() + "f, " +
                matrix4f.m30() + "f, " + matrix4f.m31() + "f, " + matrix4f.m32() + "f, " + matrix4f.m33() + "f)";
     }

    @SubscribeEvent
    public static void onKeyPressed(InputEvent.Key event) {
        if (event.getAction() != InputConstants.PRESS) {
            return;
        }
        int key = event.getKey();

        onKeyPressed(key, true);
    }

}
