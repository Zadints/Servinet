package org.example.servinet.application.dto;

import org.example.servinet.domain.entities.Payment;

import java.util.List;
import java.util.stream.Stream;

public class PaymentsScore {
    private int countPendingPayments;
    private int countPaidPayments;
    private List<Payment> PendingPayments;
    private List<Payment> paidPayments;
    private Payment ActualPayments;
    private List<Payment> allPayments;

    public PaymentsScore(List<Payment> pendingPayments, List<Payment> paidPayments, Payment actualPayments) {
        PendingPayments = pendingPayments;
        this.paidPayments = paidPayments;
        ActualPayments = actualPayments;

        countPendingPayments = pendingPayments.size();
        countPaidPayments = paidPayments.size();

        allPayments = Stream.concat(paidPayments.stream(), pendingPayments.stream()).toList();
    }

    public void addNewPayments(Payment newPaid) {
        this.ActualPayments = newPaid;
        this.PendingPayments.add(newPaid);
        this.allPayments.add(newPaid);
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
        return PendingPayments;
    }

    public List<Payment> getPaidPayments() {
        return paidPayments;
    }

    public Payment getActualPayments() {
        return ActualPayments;
    }
}
