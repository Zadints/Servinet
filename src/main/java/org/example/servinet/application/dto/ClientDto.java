package org.example.servinet.application.dto;

import org.example.servinet.domain.entities.ClientPlan;

/*
 * :C mudé de nombre ClientDto a OrderCreateDto
 *  por el tema de técnicos y ventas
 */
public record ClientDto(

        String dni,
        String firstName,
        String lastName,
        String phone,
        String address,
        String email,
        String whatsapp,
        ClientPlan plan,
        String antennaUuid,
        Double latitude,
        Double longitude
) {}