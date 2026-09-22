package org.example.servinet.core.application.dto;

import java.util.List;

public class RucDataDto {
    private String ruc;
    private String razonSocial;
    private String nombreComercial;
    private List<String> telefonos;
    private String estado;
    private String condicion;
    private String direccion;
    private String departamento;
    private String provincia;
    private String distrito;
    private String ubigeo;
    private String capital;

    public RucDataDto(String ruc, String razonSocial, String estado, String condicion, String direccion, String provincia, String distrito, String ubigeo, String capital, String departamento, List<String> telefonos, String nombreComercial) {
        this.ruc = ruc;
        this.razonSocial = razonSocial;
        this.estado = estado;
        this.condicion = condicion;
        this.direccion = direccion;
        this.provincia = provincia;
        this.distrito = distrito;
        this.ubigeo = ubigeo;
        this.capital = capital;
        this.departamento = departamento;
        this.telefonos = telefonos;
        this.nombreComercial = nombreComercial;
    }


    public String getRuc() {
        return ruc;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public List<String> getTelefonos() {
        return telefonos;
    }

    public String getEstado() {
        return estado;
    }

    public String getCondicion() {
        return condicion;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getProvincia() {
        return provincia;
    }

    public String getDepartamento() {
        return departamento;
    }

    public String getDistrito() {
        return distrito;
    }

    public String getCapital() {
        return capital;
    }

    public String getUbigeo() {
        return ubigeo;
    }
}
