package org.example.servinet.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryMovement(
        UUID movementUuid,
        UUID itemUuid,
        String itemName,
        String itemCode,
        String movementType,
        BigDecimal quantity,
        BigDecimal previousStock,
        BigDecimal newStock,
        String description,
        UUID staffUuid,
        UUID orderUuid,
        LocalDateTime createdAt
) {}