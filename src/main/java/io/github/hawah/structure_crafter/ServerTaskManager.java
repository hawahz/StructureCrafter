package io.github.hawah.structure_crafter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Supplier;

public class ServerTaskManager {
    public static Queue<ServerTask> serverTasks = new ArrayDeque<>();
    public static ServerTask createTask(Supplier<Boolean> checker, Runnable task) {
        return createTask(checker, task, 1);
    }

    public static ServerTask createTask(Supplier<Boolean> checker, Runnable task, int cycleTicks) {
        return createTask(checker, task, cycleTicks, 10/cycleTicks + 1);
    }

    public static ServerTask createTask(Supplier<Boolean> checker, Runnable task, int cycleTicks, int discardCycles) {
        ServerTask serverTask = new ServerTask(checker, task, cycleTicks, discardCycles);
        serverTasks.add(serverTask);
        return serverTask;
    }

    public static void tick() {
        List<ServerTask> toRemove = new ArrayList<>();
        for (ServerTask task : serverTasks) {
            if(task.tick()) {
                toRemove.add(task);
            }
        }
        toRemove.forEach(task -> serverTasks.remove(task));
    }

    public static class ServerTask {

        public final Supplier<Boolean> checker;
        public final Runnable task;
        public final int cycleTicks;
        public final int discardCycles;
        public int ticks = 0;
        public int cycles = 0;
        public boolean killed = false;

        protected ServerTask(Supplier<Boolean> checker, Runnable task, int cycleTicks, int discardCycles) {
            this.checker = checker;
            this.task = task;
            this.cycleTicks = cycleTicks;
            this.discardCycles = discardCycles;
        }

        public boolean tick() {
            if (killed)
                return true;
            if (ticks >= cycleTicks) {
                ticks = 0;
                cycles++;
                if (!checker.get()) {
                    return timeout();
                }
                runTask();
                killed = true;
                return true;
            }
            ticks++;
            return timeout();
        }

        public boolean timeout() {
            return cycles >= discardCycles;
        }
        public void runTask() {
            task.run();
        }
    }
}
