package org.example.servinet.core.domain.entities;

import java.time.LocalDateTime;

public record LogEntry(String userName, String type, String information, LocalDateTime createAt) {
}