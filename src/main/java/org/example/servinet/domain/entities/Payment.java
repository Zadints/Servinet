package org.example.servinet.domain.entities;
import java.math.BigDecimal;
import javafx.scene.image.Image;
import org.example.servinet.domain.enums.PayMethods;

import java.time.LocalDateTime;
import java.time.Month;

public class Payment {

    private final String id;
    private final String clientDni;
    private final BigDecimal amount;

    private LocalDateTime datePaidUser;
    private LocalDateTime createAt;

    private PayMethods payMethod;
    private Boolean paid;
    private byte[] image;

    private String description;
    private String reference;


    public Payment(String id, String clientDni, BigDecimal amount, LocalDateTime datePaidUser, LocalDateTime createAt, PayMethods payMethod, Boolean paid, byte[] image, String description, String reference) {
        this.id = id;
        this.clientDni = clientDni;
        this.amount = amount;
        this.datePaidUser = datePaidUser;
        this.createAt = createAt;
        this.payMethod = payMethod;
        this.paid = paid;
        this.image = image;
        this.description = description;
        this.reference = reference;
    }

    public String getId() {
        return id;
    }

    public String getClientDni() {
        return clientDni;
    }

    public BigDecimal getAmount() {
        return amount;
    }


    public LocalDateTime getDatePaidUser() {
        return datePaidUser;
    }

    public PayMethods getPayMethod() {
        return payMethod;
    }

    public Boolean isPaid() {
        return paid;
    }

    public byte[] getImage() {
        return image;
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }

    public void setPayMethod(PayMethods payMethod) {
        this.payMethod = payMethod;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setDatePaidUser(LocalDateTime datePaidUser) {
        this.datePaidUser = datePaidUser;
    }
}
