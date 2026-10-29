package org.example.servinet.application.dto;

import org.example.servinet.domain.enums.PayMethods;

public record PaidPaymentDto(
        byte[] image,
        PayMethods payMethod

) {


}
