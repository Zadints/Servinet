package org.example.servinet.application.dto;

import org.example.servinet.domain.entities.Payment;

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
        this.pendingPayments = pendingPayments;
        this.paidPayments = paidPayments;


        countPendingPayments = pendingPayments.size();
        countPaidPayments = paidPayments.size();

        allPayments = Stream.concat(paidPayments.stream(), pendingPayments.stream()).toList();
    }

    public String getDni() {
        return dni;
    }

    public void addNewPayment(Payment newPaid) {
        this.pendingPayments.add(newPaid);
        this.allPayments.add(newPaid);
    }

    public boolean addPaidPayment(Payment newPaid) {
        boolean removed = pendingPayments.removeIf(
                s -> s.getId().equals(newPaid.getId())
        );

        if (!removed) return false;

        this.paidPayments.add(newPaid);
        return true;
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
