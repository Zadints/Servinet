package org.example.servinet.core.domain.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Contrato {

    //datos del cliente y del servicio
    private String NombreCompleto, dni,direccion,sector,plan,antena,telefono;
    private LocalDate fechaInstalacion;
    private String rutaFotoRouter;

    // datos del primer pago
    private String mesPago, metodoPago, observacion;
    private LocalDate fechaPago;
    private double monto;

    public Contrato () {}

    public Contrato(String nombreCompleto, String dni, String telefono, String direccion,String sector, String plan, String antena, LocalDate fechaInstalacion,
                    String rutaFotoRouter, String mesPago, double monto, LocalDate fechaPago,String metodoPago, String observacion) {
        this.NombreCompleto = nombreCompleto;
        this.dni = dni;
        this.telefono = telefono;
        this.direccion = direccion;
        this.sector = sector;
        this.plan = plan;
        this.antena = antena;
        this.fechaInstalacion = fechaInstalacion;
        this.rutaFotoRouter = rutaFotoRouter;
        this.mesPago = mesPago;
        this.monto = monto;
        this.fechaPago = fechaPago;
        this.metodoPago = metodoPago;
        this.observacion = observacion;
    }

    //getter y setters
    public String getNombreCompleto() { return NombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.NombreCompleto = nombreCompleto; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public String getAntena() { return antena; }
    public void setAntena(String antena) { this.antena = antena; }

    public LocalDate getFechaInstalacion() { return fechaInstalacion; }
    public void setFechaInstalacion(LocalDate fechaInstalacion) { this.fechaInstalacion = fechaInstalacion; }

    public String getRutaFotoRouter() { return rutaFotoRouter; }
    public void setRutaFotoRouter(String rutaFotoRouter) { this.rutaFotoRouter = rutaFotoRouter; }

    public String getMesPago() { return mesPago; }
    public void setMesPago(String mesPago) { this.mesPago = mesPago; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public LocalDate getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDate fechaPago) { this.fechaPago = fechaPago; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}
