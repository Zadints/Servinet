package org.example.servinet.application.dto;

import org.example.servinet.domain.enums.InventoryCategory;
import org.example.servinet.domain.enums.StockStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record InventoryItem(
        UUID uuid,
        String code,
        String name,
        String description,
        InventoryCategory category,
        String brand,
        String model,
        boolean recoverable,
        String unit,
        BigDecimal stock,
        BigDecimal minimumStock,
        BigDecimal purchasePrice,
        boolean active,
        StockStatus status
) {}
