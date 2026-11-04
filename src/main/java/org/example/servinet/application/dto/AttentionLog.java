package org.example.servinet.application.dto;

import org.example.servinet.domain.enums.AttentionType;

public record AttentionLog(
        String staffUuid,
        String clientDni,
        String clientName,
        String clientLastName,
        AttentionType atencionTipo,
        int valoracion,
        String descriptionTipo
) {}
