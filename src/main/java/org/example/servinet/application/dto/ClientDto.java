package org.example.servinet.application.dto;

import org.example.servinet.domain.entities.ClientPlan;

/*
 * :C mudé de nombre ClientDto a OrderCreateDto
 *  por el tema de técnicos y ventas
 */
public class ClientDto {

    /*
    * Sector para rellenar tabla de clientes
    */
    private String dni;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String email;
    private String whatsapp;
    /*
     * Id del plan que solicito el cliente
     */
    private ClientPlan plan;

    public ClientDto(String dni, String firstName, String lastName, String phone, String address, String email, String whatsapp, ClientPlan plan) {
        this.dni = dni;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
        this.email = email;
        this.whatsapp = whatsapp;
        this.plan = plan;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public String getDni() { return dni; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public ClientPlan getPlan() { return plan; }
}