package io.github.hawah.structure_crafter.client.gui.utils;

import java.util.ArrayList;
import java.util.List;

public class ButtonGroup {

    public void disable() {
        this.disable = true;
    }

    private boolean disable = false;

    List<TextureToggleButton> buttons = new ArrayList<>();
    public void addButton(TextureToggleButton button) {
        buttons.add(button);
        var prevPress = button.onPress;
        button.onPress = () -> {
            if (disable) {
                return;
            }
            prevPress.run();
            announceChange(button);
        };
    }

    public void announceChange(TextureToggleButton source) {
        if (disable) {
            return;
        }
        for (TextureToggleButton button : buttons) {
            if (button.equals(source)) {
                continue;
            }
            button.setToggled(false);
        }
    }
}
