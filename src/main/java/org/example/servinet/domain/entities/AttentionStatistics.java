package org.example.servinet.domain.entities;

import java.math.BigDecimal;

public record AttentionStatistics(

        String staffUuid,
        int totalAtenciones,
        BigDecimal promedioValoracion,
        int totalVentas,
        int totalInformes,
        int totalSoportes,
        int totalSuspensiones,
        int totalCobros,
        int totalAtencionesCliente
) {}
