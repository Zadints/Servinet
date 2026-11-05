package org.example.servinet.domain.entities;

import org.example.servinet.domain.enums.LogType;

import java.time.LocalDateTime;

public record LogEntry(
        String userName, 
        LogType type,
        String information,
        LocalDateTime createAt
) {
}