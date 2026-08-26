package org.example.servinet.core.enums;

public enum ExceptionType {
    ServerNotFoundExceptionStarting("Error no se pudo encontrar la carpeta de servidor para comenzar la ejecución "),
    ServerNotFoundException("Error no se pudo encontrar la carpeta del servidor"),
    ServerInvalidPathException("Ruta especificada inválida"),
    MineIDEGeneralException("Ocurrió un error inesperado"),
    MineIDEInternalFileException("Error interno al intentar leer los datos para ejecutar el IDE"),
    CustomException("");

    //agregar más errores
    private final String error;

    ExceptionType(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return error;
    }
}
