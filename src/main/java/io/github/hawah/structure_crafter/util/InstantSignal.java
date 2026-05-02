package io.github.hawah.structure_crafter.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class InstantSignal {
    private final List<Consumer<Object[]>> listeners = new ArrayList<>();
    private final int argNum;
    public InstantSignal bind(Consumer<Object[]> listener) {
        listeners.add(listener);
        return this;
    }

    public InstantSignal unbind(Consumer<Object[]> listener) {
        listeners.remove(listener);
        return this;
    }

    public InstantSignal(int argNum) {
        this.argNum = argNum;
    }

    public void emit(Object... args) {
        if (args.length != argNum) {
            throw new IllegalArgumentException("Incorrect number of arguments");
        }
        for (Consumer<Object[]> listener : listeners) {
            listener.accept(args);
        }
    }
}
