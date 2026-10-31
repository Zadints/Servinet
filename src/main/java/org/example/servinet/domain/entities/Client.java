package org.example.servinet.domain.entities;

import javafx.scene.image.Image;
import org.example.servinet.application.dto.PaymentsScore;
import org.example.servinet.domain.enums.ClientStatus;
import org.example.servinet.domain.enums.PlanStatusClient;

import java.time.LocalDateTime;
import java.util.List;

public class Client{

    private String dni;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String whatsapp;
    private String address;
    private ClientPlan plan;
    private LocalDateTime installationDate;
    private LocalDateTime dateCreate;
    private ClientStatus clientStatus;  //ve si se retiro mijines o sigue cinsumiendo inter de la empresa
    private PlanStatusClient planStatus; //ve el estado de pago del client
    private Image routerImage;

    private PaymentsScore payments;


    public Client(String dni, String email, String firstName, String lastName, String phone, String whatsapp, String address, ClientPlan plan, LocalDateTime installationDate, LocalDateTime dateCreate, ClientStatus clientStatus, PlanStatusClient planStatus, Image routerImage, PaymentsScore payments) {
        this.dni = dni;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.whatsapp = whatsapp;
        this.address = address;
        this.plan = plan;
        this.installationDate = installationDate;
        this.dateCreate = dateCreate;
        this.clientStatus = clientStatus;
        this.planStatus = planStatus;
        this.routerImage = routerImage;
        this.payments = payments;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    public String getDni() {
        return dni;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public ClientPlan getPlan() {
        return plan;
    }

    public LocalDateTime getInstallationDate() {
        return installationDate;
    }

    public LocalDateTime getDateCreate() {
        return dateCreate;
    }

    public ClientStatus getClientStatus() {
        return clientStatus;
    }

    public PlanStatusClient getPlanStatus() {
        return planStatus;
    }

    public Image getRouterImage() {
        return routerImage;
    }


    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    public void setAddress(String address) {
        this.address = address;
    }

    public void setPlan(ClientPlan plan) {
        this.plan = plan;
    }

    public void setInstallationDate(LocalDateTime installationDate) {
        this.installationDate = installationDate;
    }

    public void setClientStatus(ClientStatus clientStatus) {
        this.clientStatus = clientStatus;
    }

    public void setPlanStatus(PlanStatusClient planStatus) {
        this.planStatus = planStatus;
    }

    public void setRouterImage(Image routerImage) {
        this.routerImage = routerImage;
    }


    public int getScorePayment() {

        List<Payment> allPayments = getAllPayments();

        if (allPayments.isEmpty()) {
            return planStatus == PlanStatusClient.AL_DIA ? 100 : 0;
        }

        int pagados = getCountPaidPayments();

        return (int) Math.round(
                (pagados * 100.0) / allPayments.size()
        );
    }

    public List<Payment> getAllPayments() {
        return payments != null
                ? payments.getAllPayments()
                : List.of();
    }

    public List<Payment> getPaidPayments() {
        return payments != null
                ? payments.getPaidPayments()
                : List.of();
    }

    public List<Payment> getPenddingPayments() {
        return payments != null
                ? payments.getPendingPayments()
                : List.of();
    }

    public int getCountPendingPayments() {
        return payments != null
                ? payments.getCountPendingPayments()
                : 0;
    }

    public int getCountPaidPayments() {
        return payments != null
                ? payments.getCountPaidPayments()
                : 0;
    }

    public void setPayments(PaymentsScore payments) {
        this.payments = payments;
    }

    public void addNewPayments(Payment payments) {
        this.payments.addNewPayment(payments);
    }
}