package io.github.hawah.structure_crafter.lib.client.render.toolkit;

import io.github.hawah.structure_crafter.lib.client.utils.AnimationTickHolder;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@EventBusSubscriber(value = Dist.CLIENT)
public class AnimationPlayer {

    private static final List<AnimationPlayer> players = new ArrayList<>();

    private double lastTickTime = 0;
    private double initialTickTime = -1;
    private boolean playing = false;
    private boolean paused = false;
    private boolean ignorePaused = false;
    private double totalLength = 0;
    private boolean cycle = false;
    private final Map<String, Animation<?>> animations = new HashMap<>();

    public AnimationPlayer() {
        players.add(this);
    }
    public <T> Animation<T> registerAnimation(String name, Function<T, T> modifier, Animation.LerpFunction<T> lerp, T initialValue) {
        Animation<T> animation = new Animation<>(modifier, lerp, initialValue, this);
        animations.put(name, animation);
        return animation;
    }

    public Animation<?> getAnimation(String name) {
        return animations.get(name);
    }

    public AnimationPlayer ignorePaused(boolean flag) {
        ignorePaused = flag;
        return this;
    }

    public AnimationPlayer cycle(boolean flag) {
        cycle = flag;
        return this;
    }

    public boolean ignorePaused() {
        return ignorePaused;
    }

    public void play() {
        initialTickTime = AnimationTickHolder.getRenderTime(ignorePaused);
        lastTickTime = initialTickTime;
        playing = true;
    }

    public boolean isPlaying() {
        return playing;
    }

    public boolean isPaused() {
        return paused;
    }

    public void pause() {
        paused = true;
    }

    public void stop() {
        playing = false;
        paused = false;
    }

    public void unpause() {
        paused = false;
    }

    public void reset() {
        initialTickTime = AnimationTickHolder.getRenderTime(ignorePaused);
    }

    public void update() {
        if (!playing) {
            return;
        }
        double currentTime = AnimationTickHolder.getRenderTime(ignorePaused);
        if (paused) {
            initialTickTime += currentTime - lastTickTime;
            lastTickTime = currentTime;
            return;
        }
        for (Animation<?> animation : animations.values()) {
            animation.value(currentTime - initialTickTime);
        }
        lastTickTime = currentTime;
        if (!(currentTime - initialTickTime > totalLength)) {
            return;
        }
        if (cycle) {
            initialTickTime += totalLength;
        } else {
            stop();
        }
    }

    public void expand(double length) {
        totalLength = Math.max(totalLength, length);
    }

    public static void updateAll() {
        players.forEach(AnimationPlayer::update);
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        AnimationPlayer.updateAll();
    }
}
