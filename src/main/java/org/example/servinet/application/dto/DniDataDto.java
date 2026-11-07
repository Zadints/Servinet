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

    public String getFullName() {
        return nombres + apellidoPaterno + " " + apellidoMaterno;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return  apellidoPaterno + " " + apellidoMaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
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