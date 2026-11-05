package org.example.servinet.domain.entities.client;

import org.example.servinet.application.dto.PaymentsScore;
import org.example.servinet.domain.enums.ClientStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Client{

    /**
     * ¿como es el asunto?
     * atencion -> es una tabla que guarda cada venta pro parte de vendedores para puntos
     * plans -> nunca se elimina colo cambia su propiedad plan_isactive segun
     *          su tiemp ode vida y queda con clientes viejos derrepente o
     *          actualizas depende de cada cricterio
     *
     *  clients -> la cabeza de todo porque es la dependencia de payments. esta tabla
     *          tiene una celda que guarda el uudi del orden de -> orders
     *  orders -> es idependiente de cliente poque si cliente se elimina este sigue viviendo como
     *          historial.
     *
     *  orders_report -> reporte generado pro el técnico desde medios externos.
     *              nada que ver con ñlógica de esta app solo apra mostrar tecnicos
     *
     **/

    private final String dni;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String email;
    private String whatsapp;
    private final LocalDateTime clientRegister;

    private ClientStatus clientStatus;

    private final Double latitude;
    private final Double longitude;

    private Order order;

    private PaymentsScore paymentsScore;

    public Client(String dni, String firstName, String lastName, String phone, String address, String email, String whatsapp, LocalDateTime clientRegister, ClientStatus clientStatus, Double latitude, Double longitude, Order order, PaymentsScore paymentsData) {
        this.dni = dni;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
        this.email = email;
        this.whatsapp = whatsapp;
        this.clientRegister = clientRegister;
        this.clientStatus = clientStatus;
        this.latitude = latitude;
        this.longitude = longitude;
        this.order = order;
        this.paymentsScore = paymentsData != null
                ? paymentsData
                : new PaymentsScore(dni, null, null);
    }

    public String getDni() {
        return dni;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public LocalDateTime getClientRegister() {
        return clientRegister;
    }

    public ClientStatus getClientStatus() {
        return clientStatus;
    }

    public Order getOrder() {
        return order;
    }
    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }



    public PaymentsScore getPaymentsScore() {
        return paymentsScore;
    }

    public String getFullName(){
        return firstName + lastName;
    }

    public List<Payment> getAllPayments() {

        List<Payment> payments = paymentsScore.getAllPayments();
        if ( payments == null) return new ArrayList<>();

        return payments;
    }

    public List<Payment> getPaidPayments() {
        return paymentsScore.getPaidPayments();
    }

    public List<Payment> getPenddingPayments() {
        return paymentsScore.getPendingPayments();
    }

    public int getCountPendingPayments() {
        return paymentsScore.getCountPendingPayments();
    }

    public int getCountPaidPayments() {
        return paymentsScore.getCountPaidPayments();
    }

    /**
     *  Entra al if en caso se crea un nuevo pago. si es uan recarga de el mes normal se agrega y listo
     **/
    public void addPaidPayment(Payment payments) {

        if (paymentsScore == null){
            paymentsScore = new PaymentsScore(
                    dni,
                    null,
                    null
            );
        }

        paymentsScore.addPaidPayment(payments);
    }
    public void addPendingPayment(Payment payments) {

        if (paymentsScore == null){
            paymentsScore = new PaymentsScore(
                    dni,
                    null,
                    null
            );
        }

        paymentsScore.addNewPayment(payments);
    }

    public void setClientStatus(ClientStatus clientStatus) {
        this.clientStatus = clientStatus;
    }
}