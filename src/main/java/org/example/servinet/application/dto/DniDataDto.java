package org.example.servinet.application.dto;

public class DniDataDto {

    private String dni;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private int codVerifica;

    public DniDataDto() {
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public int getCodVerifica() {
        return codVerifica;
    }

    public void setCodVerifica(int codVerifica) {
        this.codVerifica = codVerifica;
    }
}