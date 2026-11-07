package org.example.servinet.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record InventoryMovementResult(
        UUID movementUuid,
        BigDecimal previousStock,
        BigDecimal newStock
) {}