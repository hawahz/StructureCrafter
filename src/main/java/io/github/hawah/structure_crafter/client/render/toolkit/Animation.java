package io.github.hawah.structure_crafter.client.render.toolkit;

import net.minecraft.util.Mth;

import java.util.*;
import java.util.function.Function;

public class Animation<T> {
    private final Function<T, T> modifier;
    private final LerpFunction<T> lerp;
    private final List<Value<T>> values = new ArrayList<>();
    private T value = null;
    private double timelineLength = 0;
    private double offset = 0;
    private int animePtrBuffer = 0;
    private boolean cycle = false;
    private boolean rewind = false;
    private final AnimationPlayer player;

    public Animation(Function<T, T> modifier, LerpFunction<T> lerp, T initialValue, AnimationPlayer player) {
        this.modifier = modifier;
        this.lerp = lerp;
        this.player = player;
        setValue(initialValue);
    }

    public Animation<T> cycle(boolean flag) {
        cycle = flag;
        return this;
    }

    public Animation<T> rewind(boolean flag) {
        rewind = flag;
        return this;
    }

    public Animation<T> offset(double offset) {
        this.offset = offset;
        player.expand(timelineLength + offset);
        return this;
    }

    public void setValue(T value) {
        this.value = modifier.apply(value);
    }

    public T getValue() {
        return value;
    }

    public double getTimelineLength() {
        return timelineLength;
    }

    public Value<T> addKeyFrame(double tickTime, T value) {
        Value<T> element = Value.of(tickTime, value);
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).time() > tickTime) {
                values.add(i, element);
                return element;
            }
        }
        values.add(element);
        timelineLength = Math.max(timelineLength, tickTime);
        player.expand(timelineLength + offset);
        return element;
    }

    public T value(double tickTime) {
        if (values.isEmpty()) {
            return value;
        }

        if (tickTime <= offset) {
            return values.getFirst().value();
        }

        tickTime -= offset;

        if (tickTime > timelineLength && !cycle) {
            return value;
        }
        if (rewind) {
            tickTime %= timelineLength * 2;
            tickTime = tickTime > timelineLength? timelineLength * 2 - tickTime : tickTime;
        }
        tickTime %= timelineLength;

        for (int i = animePtrBuffer; i < values.size(); i++) {
            if (outOfPhase(tickTime, i)) {
                continue;
            }
            return value;
        }
        for (int i = 0; i < animePtrBuffer; i++) {
            if (outOfPhase(tickTime, i)) {
                continue;
            }
            return value;
        }
        return value;
    }


    @SuppressWarnings("unchecked")
    public Class<T> getType() {
        return (Class<T>) value.getClass();
    }

    public static final class Value<T> {
        private final double time;
        private final T value;
        private Function<Double, Double> mapping;

        public Value(double time, T value, Function<Double, Double> mapping) {
            this.time = time;
            this.value = value;
            this.mapping = mapping;
        }

        private Value(double time, T value) {
            this(time, value, Double::doubleValue);
        }

        static <T> Value<T> of(double time, T value) {
            return new Value<>(time, value);
        }

        public Value<T> withMapping(Function<Double, Double> mapping) {
            this.mapping = mapping;
            return this;
        }

        public double time() {
            return time;
        }

        public T value() {
            return value;
        }

        public Function<Double, Double> mapping() {
            return mapping;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Value<?>) obj;
            return Double.doubleToLongBits(this.time) == Double.doubleToLongBits(that.time) &&
                    Objects.equals(this.value, that.value) &&
                    Objects.equals(this.mapping, that.mapping);
        }

        @Override
        public int hashCode() {
            return Objects.hash(time, value, mapping);
        }

        @Override
        public String toString() {
            return "Value[" +
                    "time=" + time + ", " +
                    "value=" + value + ", " +
                    "mapping=" + mapping + ']';
        }
    }

    public interface LerpFunction<T> {

        T apply(T from, T to, double delta);
    }
    private boolean outOfPhase(double time, int i) {
        if (time < values.get(i).time() || (i + 1 < values.size() && time >= values.get(i + 1).time())) {
            return true;
        }
        if (i < values.size() - 1) {
            double phaseLength = values.get(i + 1).time() - values.get(i).time();
            double phasePast = time - values.get(i).time();
            setValue(lerp.apply(
                    values.get(i).value(),
                    values.get(i + 1).value(),
                    values.get(i).mapping().apply(Mth.clamp(phasePast / phaseLength, 0, 1))
            ));
        } else {
            setValue(values.getLast().value());
        }
        animePtrBuffer = i;
        return false;
    }
}
