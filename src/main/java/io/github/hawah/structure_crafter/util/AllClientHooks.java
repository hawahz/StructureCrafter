package io.github.hawah.structure_crafter.util;

import io.github.hawah.structure_crafter.StructureCrafterClient;
import io.github.hawah.structure_crafter.client.gui.BlackboardCheckScreen;
import io.github.hawah.structure_crafter.client.gui.ScreenOpener;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;

public class AllClientHooks {
    public static boolean hasShiftDown() {
        return Screen.hasShiftDown();
    }

    public static boolean isFirstPerson() {
        return !Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }

    public static Player getPlayer() {
        return Minecraft.getInstance().player;
    }

    public static void tryOpenBlackboardScreen(Player player) {
        if (StructureCrafterClient.BLACKBOARD_HANDLER.hasSelection() && StructureCrafterClient.BLACKBOARD_HANDLER.hasCenter()) {

            if (!StructureCrafterClient.BLACKBOARD_HANDLER.isValidSize()) {
                player.displayClientMessage(
                        LangData.ERROR_AREA_TOO_LARGE.get(),
                        true
                );
            } else if (!StructureCrafterClient.BLACKBOARD_HANDLER.isValidCenter()) {
                player.displayClientMessage(
                        LangData.ERROR_ANCHOR_OUT_OF_BOUNDS.get(),
                        true
                );
            } else {
                ScreenOpener.open(new BlackboardCheckScreen());
            }
        } else {
            player.displayClientMessage(
                    !StructureCrafterClient.BLACKBOARD_HANDLER.hasSelection()?
                            LangData.INFO_NO_SELECTION.get():
                            LangData.INFO_NO_ANCHOR.get(),
                    true
            );
        }
    }
}
