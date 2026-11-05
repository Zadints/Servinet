package org.example.servinet.application.dto;

import org.example.servinet.domain.entities.client.Payment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class PaymentsScore {
    private final String dni;
    private int countPendingPayments;
    private int countPaidPayments;
    private List<Payment> pendingPayments;
    private List<Payment> paidPayments;
    private List<Payment> allPayments;

    public PaymentsScore(String dni, List<Payment> pendingPayments, List<Payment> paidPayments) {
        this.dni = dni;

        this.pendingPayments = pendingPayments != null
                ? new ArrayList<>(pendingPayments)
                : new ArrayList<>();

        this.paidPayments = paidPayments != null
                ? new ArrayList<>(paidPayments)
                : new ArrayList<>();

        this.countPaidPayments = this.paidPayments.size();
        this.countPendingPayments = this.pendingPayments.size();

        this.allPayments = new ArrayList<>();

        this.allPayments.addAll(this.paidPayments);
        this.allPayments.addAll(this.pendingPayments);
    }

    public String getDni() {
        return dni;
    }

    public void addNewPayment(Payment newPaid) {
        this.pendingPayments.add(newPaid);
        this.allPayments.add(newPaid);
    }

    public void addPaidPayment(Payment newPaid) {
        boolean removed = pendingPayments.removeIf(
                s -> s.getId().equals(newPaid.getId())
        );

        if (removed) return;

        this.paidPayments.add(newPaid);
        return;
    }

    public List<Payment> getAllPayments() {
        return allPayments;
    }

    public int getCountPendingPayments() {
        return countPendingPayments;
    }

    public int getCountPaidPayments() {
        return countPaidPayments;
    }

    public List<Payment> getPendingPayments() {
        return pendingPayments;
    }

    public List<Payment> getPaidPayments() {
        return paidPayments;
    }

}
