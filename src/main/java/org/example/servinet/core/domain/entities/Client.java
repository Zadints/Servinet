package org.example.servinet.core.domain.entities;

import javafx.scene.image.Image;
import org.example.servinet.core.domain.enums.PlanStatusClient;

import java.time.LocalDateTime;
import java.util.List;

public class Client {

    private final String dni;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String sectorUuid;
    private ClientPlan planId;
    private LocalDateTime installationDate;
    private PlanStatusClient planStatus;
    private Image routerImage;
    private List<Pay> payments;

    // Constructor
    public Client(
            String dni,
            String email,
            String firstName,
            String lastName,
            String phone,
            String sectorUuid,
            ClientPlan planId,
            LocalDateTime installationDate,
            PlanStatusClient planStatus,
            Image routerImage,
            Pay firstPayments
    ) {
        this.dni = dni;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.sectorUuid = sectorUuid;
        this.planId = planId;
        this.installationDate = installationDate;
        this.planStatus = planStatus;
        this.routerImage = routerImage;
        this.payments.add(firstPayments);
    }

    // Getters y setters
}
