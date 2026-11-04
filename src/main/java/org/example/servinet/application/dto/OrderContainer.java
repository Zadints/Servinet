package org.example.servinet.application.dto;

import org.example.servinet.domain.entities.client.Order;
import org.example.servinet.domain.entities.client.OrderReport;

public record OrderContainer(
        OrderReport orderReport,
        Order order
) {

}
