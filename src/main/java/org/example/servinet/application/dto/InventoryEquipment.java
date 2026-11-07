package org.example.servinet.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryEquipment(
        UUID equipmentUuid,
        UUID itemUuid,
        String itemCode,
        String itemName,
        String serialNumber,
        String status,
        LocalDateTime installedAt,
        UUID installedOrderUuid
) {}
