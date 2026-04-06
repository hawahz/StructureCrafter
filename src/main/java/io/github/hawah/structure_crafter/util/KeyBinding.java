package io.github.hawah.structure_crafter.util;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public enum KeyBinding {
    CTRL_ALT_R(KeyNode.CTRL, KeyNode.ALT, KeyNode.RIGHT),
    CTRL_ALT_L(KeyNode.CTRL, KeyNode.ALT, KeyNode.LEFT),
    CTRL_ALT_M(KeyNode.CTRL, KeyNode.ALT),
    CTRL_L(KeyNode.CTRL, KeyNode.LEFT),
    CTRL_R(KeyNode.CTRL, KeyNode.RIGHT),
    ALT_L(KeyNode.ALT, KeyNode.LEFT),
    ALT_R(KeyNode.ALT, KeyNode.RIGHT),
    SHIFT_L(KeyNode.SHIFT, KeyNode.LEFT),
    SHIFT_R(KeyNode.SHIFT, KeyNode.RIGHT),
    LEFT(KeyNode.LEFT),
    RIGHT(KeyNode.RIGHT),
    ;
    private final List<Action> actions = new ArrayList<>();
    private final KeyNode[] keys;

    KeyBinding(KeyNode... keys) {
        this.keys = keys;
    }
    public void bind(Action action) {
        actions.add(action);
    }

    public static boolean isConsumed() {
        for (KeyBinding binding : values()) {
            if (!binding.isActive()) {
                continue;
            }
            for (Action action : binding.actions) {
                if (action.validateDetect.get()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isActive() {
        for (KeyNode key : keys) {
            if (!key.pressed) {
                return false;
            }
        }
        return true;
    }

    public static void tick() {
        for (KeyBinding binding : values()) {
            if (!binding.isActive()) {
                continue;
            }
            for (Action action : binding.actions) {
                if (!action.validateDetect.get()) {
                    continue;
                }
                action.action.run();
                return;
            }
        }
    }

    public static class Action {
        private final Supplier<Boolean> validateDetect;
        private final Runnable action;
        private final Component description;

        protected Action(Supplier<Boolean> validateDetect, Runnable action, Component description) {
            this.validateDetect = validateDetect;
            this.action = action;
            this.description = description;
        }

        public static Action of(Supplier<Boolean> validateDetect, Runnable action, Component description) {
            return new Action(validateDetect, action, description);
        }
    }

    public static class KeyBuffer {

        protected static final KeyBuffer INSTANCE = new KeyBuffer();

        public static boolean onMousePressed(int mouseButton, boolean pressed) {
            KeyNode.update(mouseButton, pressed, 0);
            return KeyBinding.isConsumed();
        }

        public static boolean onMouseScrolled(float delta) {
            KeyNode.update(-1, false, delta);
            return KeyBinding.isConsumed();
        }
    }

    public enum KeyNode {
        CTRL((mouseButton, pressed, scroll)->Screen.hasControlDown(), 1, 4, 4, 5),
        ALT((mouseButton, pressed, scroll) -> Screen.hasAltDown(), 0, 3, 4, 5),
        SHIFT((mouseButton, pressed, scroll) -> Screen.hasShiftDown(), 3, 4, 5),
        LEFT((mouseButton, pressed, scroll) -> mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT && pressed),
        RIGHT((mouseButton, pressed, scroll) -> mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT && pressed),
        SCROLL((mouseButton, pressed, scroll) -> scroll != 0),
        ;
        private final IKeyNodeDetection detection;
        private final Supplier<List<KeyNode>> validNext;
        private boolean pressed = false;


        KeyNode(IKeyNodeDetection detection, int... validNext) {
            this.detection = detection;
            this.validNext = () -> Arrays.stream(validNext).mapToObj(KeyNode::getById).toList();
        }

        public static KeyNode getById(int id) {
            id = Math.max(0, Math.min(values().length - 1, id));
            return values()[id];
        }

        public static void update(int mouseButton, boolean pressed, float scroll) {
            for (KeyNode node : values()) {
                node.pressed = node.detection.detect(mouseButton, pressed, scroll);
            }
        }

        public List<KeyNode> getValidNext() {
            return validNext.get();
        }

        interface IKeyNodeDetection {
            boolean detect(int mouseButton, boolean pressed, float scroll);
        }
    }

}
