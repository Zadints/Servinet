package org.example.servinet.core.application.dto;

import org.example.servinet.core.domain.enums.ClientStatus;

import java.nio.file.Path;
import java.time.LocalDate;

public class ClientDto {

    private String uuid;
    private String dni;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String zone;
    private String address;
    private String planId;
    private LocalDate installationDate;
    private ClientStatus clientStatus;
    private Path routerImage;

    public ClientDto(
            String uuid,
            String dni,
            String email,
            String firstName,
            String lastName,
            String phone,
            String zone,
            String address,
            String planId,
            LocalDate installationDate,
            ClientStatus clientStatus,
            Path routerImage
    ) {
        this.uuid = uuid;
        this.dni = dni;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.zone = zone;
        this.address = address;
        this.planId = planId;
        this.installationDate = installationDate;
        this.clientStatus = clientStatus;
        this.routerImage = routerImage;
    }

    public String getUuid() { return uuid; }
    public String getDni() { return dni; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public String getZone() { return zone; }
    public String getAddress() { return address; }
    public String getPlanId() { return planId; }
    public LocalDate getInstallationDate() { return installationDate; }
    public ClientStatus getClientStatus() { return clientStatus; }
    public Path getRouterImage() { return routerImage; }
}