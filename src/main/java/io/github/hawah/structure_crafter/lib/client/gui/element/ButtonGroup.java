package io.github.hawah.structure_crafter.lib.client.gui.element;

import java.util.ArrayList;
import java.util.List;

public class ButtonGroup {

    public void disable() {
        this.disable = true;
    }

    private boolean disable = false;

    List<TextureButton> buttons = new ArrayList<>();
    public void addButton(TextureButton button) {
        buttons.add(button);
        var prevPress = button.onPress;
        button.onPress = () -> {
            if (disable) {
                return;
            }
            announceChange(button);
            prevPress.run();
        };
    }

    public void announceChange(TextureButton source) {
        if (disable) {
            return;
        }
        for (TextureButton button : buttons) {
            if (button.equals(source)) {
                continue;
            }
            button.setPressed(false);
        }
    }
}
