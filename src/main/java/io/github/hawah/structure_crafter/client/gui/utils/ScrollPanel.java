package io.github.hawah.structure_crafter.client.gui.utils;

import io.github.hawah.structure_crafter.client.gui.BaseScreen;
import io.github.hawah.structure_crafter.util.InstantSignal;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScrollPanel extends AbstractWidget {

    private final int max;
    private final int min;
    private int value = 0;
    private int pendingValue = 0;
    public final InstantSignal VALUE_CHANGED = new InstantSignal(1);
    public final InstantSignal SCROLLED = new InstantSignal(1);

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> pendingTask = null;
    private static final int DELAY_MS = 150;

    public ScrollPanel(int x, int y, int width, int height, int max, int min) {
        super(x, y, width, height, Component.empty());
        this.max = max;
        this.min = min;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        BaseScreen.drawHandwriteNumber(
                guiGraphics,
                getX(),
                getY(),
                value
        );
    }

    public void setValue(int value) {
        this.value = value;
        if (pendingTask != null && !pendingTask.isDone()) {
            pendingTask.cancel(false);
        }
        pendingValue = value;
        pendingTask = scheduler.schedule(() -> {
            // 滚动停止后才提交最终值
            if (pendingValue != this.value) {
                this.value = pendingValue;
                VALUE_CHANGED.emit(this.value);
            }
        }, 200, TimeUnit.MILLISECONDS);
    }

    public void updateInstantly() {
        if (pendingTask != null && !pendingTask.isDone()) {
            pendingTask.cancel(false);
        }
        VALUE_CHANGED.emit(this.value);
    }

    public int getValue() {
        return value;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        SCROLLED.emit(scrollY);
        if (Screen.hasShiftDown()) {
            setValue(value + (int) (scrollY * 10));
        } else {
            setValue(value + (int) scrollY);
        }
        value = Mth.clamp(value, min, max);
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
