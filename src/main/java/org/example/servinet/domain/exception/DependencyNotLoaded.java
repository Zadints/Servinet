package org.example.servinet.domain.exception;

public class DependencyNotLoaded extends RuntimeException {
    String noLoadClass;
    String dependenceClass;

    public DependencyNotLoaded(String noLoadClass, String dependenceClass) {
        this.dependenceClass = dependenceClass;
        this.noLoadClass = noLoadClass;
    }

    @Override
    public String getMessage() {
        return "No se pudo cargar " + noLoadClass + " Porque no tienes cargada los datos de "+ dependenceClass;
    }
}
