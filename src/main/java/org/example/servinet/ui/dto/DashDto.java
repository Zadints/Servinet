package org.example.servinet.ui.dto;

import java.math.BigDecimal;

public record DashDto(
        BigDecimal allMoney,
        int clientCount,
        int staffCount,
        int antennasActive

) {
}
