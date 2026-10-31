package org.example.servinet.application.dto;

import org.example.servinet.domain.entities.Antenna;

public record EditAntennaDto(
        String name,
        Double latitude,
        Double longitude,
        byte[] image,
        Antenna antenna
) {}
