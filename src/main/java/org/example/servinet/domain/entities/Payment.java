package org.example.servinet.domain.entities;
import java.math.BigDecimal;
import javafx.scene.image.Image;
import org.example.servinet.domain.enums.PayMethods;

import java.time.LocalDateTime;

public class Payment {

    private final String id;
    private final String clientDni;
    private final BigDecimal amount;
    private LocalDateTime payDate;
    private PayMethods payMethod;
    private Boolean paid;
    private Image image;

    private String description;
    private String reference;



    public Payment(String id, String clientDni, BigDecimal mont, LocalDateTime payDate, PayMethods payMethod, Boolean paid, Image image, String description, String reference) {
        this.id = id;
        this.clientDni = clientDni;
        this.amount = mont;
        this.payDate = payDate;
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

    public LocalDateTime getPayDate() {
        return payDate;
    }

    public PayMethods getPayMethod() {
        return payMethod;
    }

    public Boolean isPaid() {
        return paid;
    }

    public Image getImage() {
        return image;
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }

    public void setPayDate(LocalDateTime payDate) {
        this.payDate = payDate;
    }

    public void setPayMethod(PayMethods payMethod) {
        this.payMethod = payMethod;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }


}
