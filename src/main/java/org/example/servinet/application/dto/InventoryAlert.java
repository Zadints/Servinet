package org.example.servinet.application.dto;


import org.example.servinet.domain.enums.InventoryCategory;
import org.example.servinet.domain.enums.StockStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record InventoryAlert(
        UUID itemUuid,
        String code,
        String name,
        InventoryCategory category,
        BigDecimal stock,
        BigDecimal minimumStock,
        String unit,
        StockStatus status
) {}
