package io.github.hawah.structure_crafter.client;

public class LazySet {
    public int ticksLeft;
    public Runnable setter;

    public LazySet(int ticksLeft, Runnable setter) {
        this.ticksLeft = ticksLeft;
        this.setter = setter;
    }

    public static  LazySet create(int delay, Runnable setter) {
        return new LazySet(delay, setter);
    }

    public void tick() {
        ticksLeft--;
        if (isDiscarded()) {
            setter.run();
        }
    }

    public boolean isDiscarded() {
        return ticksLeft < 0;
    }
}
