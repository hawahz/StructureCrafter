package io.github.hawah.structure_crafter.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;

public class ScreenOpener {

    private static final Deque<Screen> backStack = new ArrayDeque<>();
    @Nullable
    private static Screen backSteppedFrom = null;

    public static void open(@Nullable Screen screen) {
        open(Minecraft.getInstance().screen, screen);
    }

    public static void open(@Nullable Screen current, @Nullable Screen toOpen) {
        backSteppedFrom = null;
        if (current != null) {
            if (backStack.size() >= 15) // don't go deeper than 15 steps
                backStack.pollLast();

            backStack.push(current);
        } else
            backStack.clear();

        openScreen(toOpen);
    }

    private static void openScreen(@Nullable Screen screen) {
        Minecraft.getInstance()
                .tell(() -> Minecraft.getInstance()
                        .setScreen(screen));
    }

}

