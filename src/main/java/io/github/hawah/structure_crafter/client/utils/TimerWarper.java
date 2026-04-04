package io.github.hawah.structure_crafter.client.utils;

import net.minecraft.client.DeltaTracker;

public class TimerWarper implements DeltaTracker {

    private float partialTick = 0;
    private float prevUnpausedPartialTick = 0;
    private float realTimeDelta = 0;
    public DeltaTracker warp(DeltaTracker timer) {
        this.realTimeDelta = timer.getRealtimeDeltaTicks();
        float delta = timer.getGameTimeDeltaPartialTick(true);
        if (delta == prevUnpausedPartialTick) {
            partialTick = partialTick + timer.getGameTimeDeltaTicks();
            partialTick -= (int) partialTick;
        } else {
            prevUnpausedPartialTick = delta;
            partialTick = delta;
        }
        return this;
    }

    @Override
    public float getGameTimeDeltaTicks() {
        return partialTick;
    }

    @Override
    public float getGameTimeDeltaPartialTick(boolean runsNormally) {
        return partialTick;
    }

    @Override
    public float getRealtimeDeltaTicks() {
        return realTimeDelta;
    }
}
