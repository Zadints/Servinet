package org.example.servinet.core.application.usecase.important;

import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppExecutor {

    // 4 hilos "ayudantes" trabajando en paralelo es más que suficiente para esta app.
    private static final ExecutorService executor = Executors.newFixedThreadPool(4, runnable -> {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true); // que no impida cerrar la app al salir
        return thread;
    });

    private AppExecutor() {
    }

    public static void submit(Task<?> task) {
        executor.submit(task);
    }

    public static void shutdown() {
        executor.shutdown();
    }
}