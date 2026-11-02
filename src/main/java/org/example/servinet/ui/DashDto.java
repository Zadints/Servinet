package org.example.servinet.ui;

import java.math.BigDecimal;

public record DashDto(
        BigDecimal allMoney,
        int clientCount,
        int staffCount,
        int antennasActive

) {
}
