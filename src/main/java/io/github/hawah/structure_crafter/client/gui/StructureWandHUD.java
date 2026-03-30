package io.github.hawah.structure_crafter.client.gui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.hawah.structure_crafter.Paths;
import io.github.hawah.structure_crafter.StructureCrafter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Stream;

@SuppressWarnings("SameParameterValue")
public class StructureWandHUD extends Screen {
    List<Component> allStructures = new ArrayList<>();
    private boolean initialized = false;
    private final float MAX_OFFSET = 15;
    private final float SPLIT_RATE = 1/3.5F;

    private final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(StructureCrafter.MODID, "textures/gui/" + "nametag" + ".png");
    private int currentStructure;
    private int oTicker;
    private int ticker = 0;
    private int animateTicker = 0;
    private final Queue<State> actions = new ArrayDeque<>();
    final int MAX_T = 4;
    private int speedMultiplier = 0;
    private State state = State.IDLE;

    public StructureWandHUD() {
        super(Component.literal("Structure Wand HUD"));
    }

    @Override
    public void init() {
        super.init();
        initialized = true;
    }
    public void loadStructures() {
        allStructures.clear();
        try (Stream<Path> paths = Files.list(Paths.STRUCTURE_DIR)) {
            paths.filter(f -> !Files.isDirectory(f) && f.getFileName().toString().endsWith(".nbt"))
                    .forEach(path -> {
                        if (Files.isDirectory(path))
                            return;

                        allStructures.add(Component.literal(path.getFileName().toString()));
                    });
        } catch (NoSuchFileException ignored) {
            // No Schematics created yet
        } catch (IOException ignored) {
        }
        currentStructure = Mth.clamp(currentStructure, 0, allStructures.size());
    }
    public void render(GuiGraphics guiGraphics, float partialTicks) {
        PoseStack poseStack = guiGraphics.pose();
        Window mainWindow = Minecraft.getInstance().getWindow();

        if (Minecraft.getInstance().screen != null) {
            return;
        }

        if (!initialized) {
            init(Minecraft.getInstance(), mainWindow.getGuiScaledWidth(), mainWindow.getGuiScaledHeight());
        }

        if (allStructures.isEmpty()) {
            return;
        }

        int x = Math.max(mainWindow.getGuiScaledWidth()  / 2 - 182 / 2 - 12 - 109, 0);
        int y = mainWindow.getGuiScaledHeight() - 36;
        animateTicker = Mth.lerpInt(partialTicks, oTicker * 20, ticker * 20);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        switch ( state) {
            case IDLE -> renderIdle(guiGraphics, poseStack, x, y);
            case SCROLLING_UP -> renderScrollUp(guiGraphics, poseStack, x, y);
            case SCROLLING_DOWN -> renderScrollDown(guiGraphics, poseStack, x, y);
            case SHOWUP -> renderShowUp(guiGraphics, poseStack, x, y, MAX_T * 20);
            default -> {
                float offset;
                renderSingleLabel(
                        guiGraphics,
                        poseStack,
                        x,
                        y,
                        offset = MAX_OFFSET * (float) Math.sin(-Math.PI*(SPLIT_RATE)),
                        offsetToScale(-offset, false),
                        allStructures.get(currentStructure + 1 < 0? currentStructure + 1 + allStructures.size() : (currentStructure + 1) % allStructures.size())
                );
                renderSingleLabel(
                        guiGraphics,
                        poseStack,
                        x,
                        y,
                        -offset,
                        offsetToScale(-offset, false),
                        allStructures.get(currentStructure - 1 < 0? currentStructure - 1 + allStructures.size() : (currentStructure - 1) % allStructures.size())
                );
                renderSingleLabel(
                        guiGraphics,
                        poseStack,
                        x,
                        y,
                        0,
                        1,
                        allStructures.get(currentStructure)
                );
            }
        }

    }

    private void renderIdle(GuiGraphics guiGraphics, PoseStack poseStack, int x, int y) {
        int delta = 1;
        float alpha = 1;
        if (animateTicker > 400) {
            delta = animateTicker - 400;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            // 设置透明度
            alpha = 1- delta/200f;

            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        }

        if (alpha >= 0) {
            renderSingleLabel(
                    guiGraphics,
                    poseStack,
                    x,
                    y,
                    0,
                    1,
                    allStructures.get(currentStructure)
            );
        }

        if (animateTicker > 400) {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.disableBlend();
        }
    }

    private void renderScrollUp(GuiGraphics guiGraphics, PoseStack poseStack, int x, int y) {
        renderScroll(guiGraphics, poseStack, x, y, MAX_T * 20);
    }

    private void renderScrollDown(GuiGraphics guiGraphics, PoseStack poseStack, int x, int y) {
        renderScroll(guiGraphics, poseStack, x, y, MAX_T * 20);
    }

    private void renderShowUp(GuiGraphics guiGraphics, PoseStack poseStack, int x, int y, int M_T) {
        float offset;

        renderSingleLabel(
                guiGraphics,
                poseStack,
                x,
                y,
                offset = Mth.lerp(animateTicker/(MAX_T * 20F), 0, MAX_OFFSET * (float) Math.sin(-Math.PI*(SPLIT_RATE))),
                offsetToScale(-offset, false),
                animateTicker < M_T * 0.6?
                        Component.empty() :
                        allStructures.get(currentStructure + 1 < 0? currentStructure + 1 + allStructures.size() : (currentStructure + 1) % allStructures.size())
        );
        renderSingleLabel(
                guiGraphics,
                poseStack,
                x,
                y,
                -offset,
                offsetToScale(-offset, false),
                animateTicker < M_T * 0.6?
                        Component.empty() :
                        allStructures.get(currentStructure - 1 < 0? currentStructure - 1 + allStructures.size() : (currentStructure - 1) % allStructures.size())
        );
        renderSingleLabel(
                guiGraphics,
                poseStack,
                x,
                y,
                0,
                1,
                allStructures.get(currentStructure)
        );
    }

    private void renderScroll(GuiGraphics guiGraphics, PoseStack poseStack, int x, int y, int M_T) {
        float offset;
        int flag = state.equals(State.SCROLLING_DOWN) ? 1 : -1;

        float progress = Math.abs(animateTicker) / (float) M_T;
        boolean isUp = animateTicker < 0;
        boolean pastHalf = progress > 0.5f;

        float center = 0;
        float top = (float) (-Math.PI * SPLIT_RATE);
        float bottom = (float) (Math.PI * SPLIT_RATE);
        float farTop = (float) (-Math.PI * (0.5 + SPLIT_RATE));
        float farBottom = (float) (Math.PI * (0.5 + SPLIT_RATE));

        // Label 1:
        // Down: 从远上(不可见) -> 移动到上(可见)
        // Up:   从上(可见)   -> 移动到远上(不可见)
        float start1 = isUp ? top : farTop;
        float end1   = isUp ? farTop : top;
        offset = MAX_OFFSET * (float) Math.sin(Mth.lerp(progress, start1, end1));

        boolean isBack1 = isUp == pastHalf;
        Component text1 = isBack1 ? Component.empty() :
                isUp? allStructures.get(currentStructure - flag * 2 < 0? currentStructure - flag * 2 + allStructures.size() * (allStructures.size() == 1? 2 : 1) : (currentStructure - flag * 2) % allStructures.size()) :
                        allStructures.get(currentStructure + flag < 0? currentStructure + flag + allStructures.size() * (allStructures.size() == 1? 2 : 1) : (currentStructure + flag) % allStructures.size());
        renderSingleLabel(guiGraphics, poseStack, x, y, offset, offsetToScale(-offset, isBack1), text1);

        // Label 2:
        // Down: 从下(可见)   -> 移动到远下(不可见)
        // Up:   从远下(不可见) -> 移动到下(可见)
        float start2 = isUp ? farBottom : bottom;
        float end2   = isUp ? bottom : farBottom;
        offset = MAX_OFFSET * (float) Math.sin(Mth.lerp(progress, start2, end2));

        boolean isBack2 = isUp != pastHalf;
        Component text2 = isBack2 ? Component.empty() :
                isUp? allStructures.get(currentStructure + flag < 0? currentStructure + flag + allStructures.size() * (allStructures.size() == 1? 2 : 1) : (currentStructure + flag) % allStructures.size()) :
                        allStructures.get(currentStructure - flag * 2 < 0? currentStructure - flag * 2 + allStructures.size() * (allStructures.size() == 1? 2 : 1) : (currentStructure - flag * 2) % allStructures.size());
        renderSingleLabel(
                guiGraphics,
                poseStack,
                x,
                y,
                offset,
                offsetToScale(offset, isBack2),
                text2
        );

        // Label 3:
        // Down: 从中央 -> 移动到下
        // Up:   从下   -> 移动到中央
        float start3 = isUp ? bottom : center;
        float end3   = isUp ? center : bottom;
        offset = MAX_OFFSET * (float) Math.sin(Mth.lerp(progress, start3, end3));
        renderSingleLabel(
                guiGraphics,
                poseStack,
                x,
                y,
                offset,
                offsetToScale(offset, false),
                isUp? allStructures.get(currentStructure) :
                        allStructures.get(currentStructure - flag < 0? currentStructure - flag + allStructures.size() : (currentStructure - flag) % allStructures.size())
        );

        // Label 4:
        // Down: 从上   -> 移动到中央
        // Up:   从中央 -> 移动到上
        float start4 = isUp ? center : top;
        float end4   = isUp ? top : center;
        offset = MAX_OFFSET * (float) Math.sin(Mth.lerp(progress, start4, end4));
        renderSingleLabel(
                guiGraphics,
                poseStack,
                x,
                y,
                offset,
                offsetToScale(-offset, false),
                state.equals(State.SCROLLING_DOWN)? allStructures.get(currentStructure) :
                        allStructures.get(currentStructure + 1 < 0? currentStructure + 1 + allStructures.size() : (currentStructure + 1) % allStructures.size())
        );
    }

    public void tick() {
        oTicker = ticker;
        switch ( state) {
            case SCROLLING_UP -> ticker -= 1+speedMultiplier;
            case SCROLLING_DOWN, SHOWUP, IDLE -> ticker+=speedMultiplier+1;
        }

        if (Screen.hasAltDown() && (state.equals(State.IDLE) || state.equals(State.SHOWUP))) {
            turnState(State.SHOWUP);
        } else if (!Screen.hasAltDown() && state.equals(State.SHOWUP)) {
            turnState(State.IDLE);
        } else if (Math.abs(ticker) > MAX_T || this.state.equals(State.REPEATER)) {
            turnState(State.valueOf(this.state.fallback));
        }
        if (state != State.IDLE) {
            ticker = Mth.clamp(ticker, -MAX_T, MAX_T);
        } else if (ticker > 2000) {
            ticker = 2000;
        }

        if (actions.isEmpty()) {
            return;
        }

        if (turnState(actions.peek())) {
            actions.poll();
        }

//        System.out.println(ticker);
    }

    public float offsetToScale(float offset, boolean isBack) {
        float v;
        if (!isBack) {
            v = (float) (Mth.inverseLerp(offset, 0.0F, MAX_OFFSET) * Math.PI/2);
        } else {
            v = (float) ((1 + Mth.inverseLerp(offset, MAX_OFFSET, 0.0F)) * Math.PI/2);
        }
        final float FINAL_SCALE = 0.9F;
        return Mth.lerp(Mth.sin(v/2), 1.0F, FINAL_SCALE);
    }

    public void renderSingleLabel(GuiGraphics guiGraphics, PoseStack poseStack, int x, int y, float offset, float scale) {
        renderSingleLabel(guiGraphics, poseStack, x, y, offset, scale, Component.empty());
    }
    public void renderSingleLabel(GuiGraphics guiGraphics, PoseStack poseStack, int x, int y, float offset, float scale, Component component) {
        poseStack.pushPose();
        poseStack.translate(x + 54.5, y + 8.5, 0);
        poseStack.scale(scale, scale, scale);
//        poseStack.mulPose(Axis.ZN.rotationDegrees(offset));
        poseStack.translate(-x - 54.5, -y - 8.5 + offset, 0);
        guiGraphics.blit(
                texture,
                x,
                y,
                69,
                213,
                109,
                17
        );
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                component,
                x + 10,
                y + 4,
                0xe3d1bc
        );
        poseStack.popPose();
    }

    public void setCurrentStructure(String currentStructure) {
        if (!allStructures.contains(Component.literal(currentStructure))) {
        }
//        this.currentStructure = allStructures.indexOf(Component.literal(currentStructure));
    }

    public String scrollUp() {
        if (!turnState(State.SCROLLING_UP)) {
            if (!State.SCROLLING_UP.equals(this.state)) {
                speedMultiplier = 0;
            }
            if (actions.size() < 4) {
                speedMultiplier ++;
            }
            return allStructures.get(currentStructure).getString();
        }
        currentStructure --;
        if (currentStructure < 0) {
            currentStructure = allStructures.size() - 1;
        }


        return allStructures.get(currentStructure).getString();
    }

    public String scrollDown() {
        if (!turnState(State.SCROLLING_DOWN)) {
            if (!State.SCROLLING_DOWN.equals(this.state)) {
                speedMultiplier = 0;
            }
            if (actions.size() < 4) {
                speedMultiplier ++;
            }
            return allStructures.get(currentStructure).getString();
        }
        currentStructure ++;
        if (currentStructure >= allStructures.size()) {
            currentStructure = 0;
        }
        return allStructures.get(currentStructure).getString();
    }

    private boolean turnState(State state) {
        if (!this.state.isValidNext(state)) {
            return false;
        }

        State oState = this.state;
        this.state = state;
        boolean turnZeroCase = (
                (state != State.REPEATER) &&
                        !((oState == State.REPEATER) && state == State.SHOWUP)
        );
        if (turnZeroCase) {
            ticker = 0;
        }
        speedMultiplier = 0;
        if ((oState == State.REPEATER) && state == State.SHOWUP) {
            ticker = MAX_T;
        }

        this.oTicker = ticker;
        return true;
    }

    enum State {
        IDLE("IDLE", "SHOWUP"),
        SCROLLING_UP("REPEATER", "REPEATER"),
        SCROLLING_DOWN("REPEATER", "REPEATER"),
        REPEATER("SHOWUP", "SCROLLING_UP", "SCROLLING_DOWN", "SHOWUP"),
        SHOWUP("IDLE", "IDLE", "SCROLLING_UP", "SCROLLING_DOWN");

        final List<String> validNext = new ArrayList<>();
        final String fallback;

        State(String fallback, String... validNext) {
            this.fallback = fallback;
            this.validNext.addAll(List.of(validNext));
        }

        public boolean isValidNext(State state) {
            return validNext.contains(state.name());
        }
    }
}
