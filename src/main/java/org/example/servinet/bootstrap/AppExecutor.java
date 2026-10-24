package org.example.servinet.bootstrap;

import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppExecutor {

    private static final int THREAD_COUNT = 4;

    private final ExecutorService EXECUTOR =
            Executors.newFixedThreadPool(THREAD_COUNT, runnable -> {
                Thread thread = new Thread(runnable);
                thread.setDaemon(true);
                return thread;
            });


    public void execute(Task<?> task) {
        EXECUTOR.execute(task);
    }

    public void shutdown() {
        EXECUTOR.shutdown();
    }
}
