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
    CTRL_ALT_S(KeyNode.CTRL, KeyNode.ALT, KeyNode.SCROLL),
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
    public final KeyNode[] keys;
    public final KeyNode[] flipedKeys;

    KeyBinding(KeyNode... keys) {
        this.keys = keys;
        flipedKeys = new KeyNode[keys.length];
        for (int i = 0; i < keys.length; i++) {
            flipedKeys[i] = keys[keys.length - i - 1];
        }
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isActive() {
        for (KeyNode key : keys) {
            if (!key.pressed) {
                return false;
            }
        }
        return true;
    }

    public boolean canDisplay() {
        for (KeyNode value : KeyNode.values()) {
            if (Arrays.asList(keys).contains(value)) {
                continue;
            }
            if (value.isActive() && !value.isEnd()) {
                return false;
            }
        }
        for (Action action : actions) {
            if (action.validateDetect.get()) {
                return true;
            }
        }
        return false;
    }

    public Component getValidDescription() {
        for (Action action : actions) {
            if (action.validateDetect.get()) {
                return action.description.get();
            }
        }
        return Component.empty();
    }

    public static void tick() {

        KeyBuffer.update();
        for (KeyBinding binding : values()) {
            if (!binding.isActive()) {
                continue;
            }
            for (Action action : binding.actions) {
                if (!action.tryActivate()) {
                    continue;
                }
                KeyBuffer.update(0, false, 0);
                return;
            }
        }
        KeyBuffer.update(0, false, 0);
    }

    public static class Action {
        private final Supplier<Boolean> validateDetect;
        private final Runnable action;
        private final Supplier<Component> description;
        private boolean activated = false;
        private final Runnable onRelease;
        public static final Runnable EMPTY = () -> {};

        protected Action(Supplier<Boolean> validateDetect, Runnable action, Component description) {
            this(validateDetect, action, description, () -> {});
        }

        protected Action(Supplier<Boolean> validateDetect, Runnable action, Supplier<Component> description) {
            this(validateDetect, action, description, () -> {});
        }

        protected Action(Supplier<Boolean> validateDetect, Runnable action, Component description, Runnable onRelease) {
            this(validateDetect, action, () -> description, onRelease);
        }

        protected Action(Supplier<Boolean> validateDetect, Runnable action, Supplier<Component> description, Runnable onRelease) {
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

        public static Action of(Supplier<Boolean> validateDetect, Runnable action, Supplier<Component> description) {
            return new Action(validateDetect, action, description);
        }

        public static Action of(Supplier<Boolean> validateDetect, Runnable action, Supplier<Component> description, Runnable onRelease) {
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
            return !action.equals(EMPTY);
        }
    }

    public static class KeyBuffer {
        public static double scroll = 0;
        private static int oMouseButton = -1;
        private static boolean oPressed = false;
        private static double oDelta;

        public static boolean onMousePressed(int mouseButton, boolean pressed) {
            update(mouseButton, pressed, 0);
            return KeyBinding.isConsumed();
        }

        public static boolean onMouseScrolled(double delta) {
            update(-1, false, delta);
            return KeyBinding.isConsumed();
        }

        public static void update(int mouseButton, boolean pressed, double delta) {
            KeyNode.update(mouseButton, pressed, delta);
            oMouseButton = mouseButton;
            oPressed = pressed;
            oDelta = delta;
        }
        public static void update() {
            KeyNode.update(oMouseButton, oPressed, oDelta);
        }

        public static int getIntDelta() {
            return (int) (scroll > 0? Math.ceil(scroll): Math.floor(scroll));
        }
    }

    public enum KeyNode {
        CTRL((mouseButton, pressed, scroll)->Screen.hasControlDown(), false, 1, 4, 4, 5),
        ALT((mouseButton, pressed, scroll) -> Screen.hasAltDown(), false, 0, 3, 4, 5),
        SHIFT((mouseButton, pressed, scroll) -> Screen.hasShiftDown(), false, 3, 4, 5),
        LEFT((mouseButton, pressed, scroll) -> mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT && pressed, true),
        RIGHT((mouseButton, pressed, scroll) -> mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT && pressed, true),
        SCROLL((mouseButton, pressed, scroll) -> scroll != 0, true),
        ;
        private final IKeyNodeDetection detection;
        private final boolean isEnd;
        private final Supplier<List<KeyNode>> validNext;
        private boolean pressed = false;


        KeyNode(IKeyNodeDetection detection, boolean isEnd, int... validNext) {
            this.detection = detection;
            this.isEnd = isEnd;
            this.validNext = () -> Arrays.stream(validNext).mapToObj(KeyNode::getById).toList();
        }

        public boolean isActive() {
            return pressed;
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

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public boolean isEnd() {
            return isEnd;
        }

        interface IKeyNodeDetection {
            boolean detect(int mouseButton, boolean pressed, double scroll);
        }
    }

}
