package org.example.servinet.core.domain.entities;

import javafx.scene.image.Image;
import org.example.servinet.core.domain.enums.ClientStatus;
import org.example.servinet.core.domain.enums.PlanStatusClient;
import org.example.servinet.core.domain.repository.Identifiable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Client{

    private String dni;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private ClientPlan plan;
    private LocalDateTime installationDate;
    private LocalDateTime dateCreate;
    private ClientStatus clientStatus;  //ve si se retiro mijines o sigue cinsumiendo inter de la empresa
    private PlanStatusClient planStatus; //ve el estado de pago del client
    private Image routerImage;
    private List<Pay> payments;

    public Client(
            String dni,
            String email,
            String firstName,
            String lastName,
            String phone,
            String address,
            ClientPlan plan,
            LocalDateTime installationDate,
            LocalDateTime dateCreate,
            ClientStatus clientStatus,
            PlanStatusClient planStatus,
            Image routerImage
    ) {
        this.dni = dni;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
        this.plan = plan;
        this.installationDate = installationDate;
        this.dateCreate = dateCreate;
        this.clientStatus = clientStatus;
        this.planStatus = planStatus;
        this.routerImage = routerImage;
        this.payments = new ArrayList<>();
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

    public List<Pay> getPayments() {
        return payments;
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

    public void addPayment(Pay pay) {
        this.payments.add(pay);
    }

    public int getScorePayment() {
        if (payments == null || payments.isEmpty()) {
            return planStatus == PlanStatusClient.AL_DIA ? 100 : 0;
        }

        long pagados = payments.stream().filter(Pay::getPaid).count();
        return (int) Math.round((pagados * 100.0) / payments.size());
    }
}