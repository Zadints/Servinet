package org.example.servinet.core.application.usecase;

import org.example.servinet.core.domain.entities.Log;
import org.example.servinet.core.domain.enums.LogType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogsUseCase {
    private static List<Log> logs = Collections.synchronizedList(new ArrayList<>());
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void addLog(LogType type) {
        executor.submit(() -> {


        });
    }
}
