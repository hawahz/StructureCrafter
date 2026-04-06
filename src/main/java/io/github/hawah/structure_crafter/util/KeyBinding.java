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
    CTRL_ALT_S(KeyNode.CTRL, KeyNode.ALT),
    CTRL_L(KeyNode.CTRL, KeyNode.LEFT),
    CTRL_R(KeyNode.CTRL, KeyNode.RIGHT),
    CTRL_S(KeyNode.CTRL, KeyNode.SCROLL),
    ALT_L(KeyNode.ALT, KeyNode.LEFT),
    ALT_R(KeyNode.ALT, KeyNode.RIGHT),
    ALT_S(KeyNode.ALT, KeyNode.SCROLL),
    SHIFT_L(KeyNode.SHIFT, KeyNode.LEFT),
    SHIFT_R(KeyNode.SHIFT, KeyNode.RIGHT),
    SHIFT_S(KeyNode.SHIFT, KeyNode.SCROLL),
    LEFT(KeyNode.LEFT),
    RIGHT(KeyNode.RIGHT),
    CTRL(KeyNode.CTRL),
    ALT(KeyNode.ALT),
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
                if (!action.tryActivate()) {
                    continue;
                }
                KeyNode.update(0, false, 0);
                return;
            }
        }
    }

    public static class Action {
        private final Supplier<Boolean> validateDetect;
        private final Runnable action;
        private final Component description;
        private boolean activated = false;
        private final Runnable onRelease;

        protected Action(Supplier<Boolean> validateDetect, Runnable action, Component description) {
            this(validateDetect, action, description, () -> {});
        }

        protected Action(Supplier<Boolean> validateDetect, Runnable action, Component description, Runnable onRelease) {
            this.validateDetect = validateDetect;
            this.action = action;
            this.description = description;
            this.onRelease = onRelease;
        }

        public static Action of(Supplier<Boolean> validateDetect, Runnable action, Component description) {
            return new Action(validateDetect, action, description);
        }

        public static Action of(Supplier<Boolean> validateDetect, Runnable action, Component description, Runnable onRelease) {
            return new Action(validateDetect, action, description, onRelease);
        }

        public boolean tryActivate() {
            if (!validateDetect.get()) {
                if (activated) {
                    onRelease.run();
                }
                return false;
            }
            activated = true;
            action.run();
            return true;
        }
    }

    public static class KeyBuffer {
        public static double scroll = 0;

        public static boolean onMousePressed(int mouseButton, boolean pressed) {
            KeyNode.update(mouseButton, pressed, 0);
            return KeyBinding.isConsumed();
        }

        public static boolean onMouseScrolled(double delta) {
            KeyNode.update(-1, false, delta);
            return KeyBinding.isConsumed();
        }

        public static int getIntDelta() {
            return (int) (scroll > 0? Math.ceil(scroll): Math.floor(scroll));
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

        public static void update(int mouseButton, boolean pressed, double scroll) {
            for (KeyNode node : values()) {
                node.pressed = node.detection.detect(mouseButton, pressed, scroll);
            }
            KeyBuffer.scroll = scroll;
        }

        public List<KeyNode> getValidNext() {
            return validNext.get();
        }

        interface IKeyNodeDetection {
            boolean detect(int mouseButton, boolean pressed, double scroll);
        }
    }

}
