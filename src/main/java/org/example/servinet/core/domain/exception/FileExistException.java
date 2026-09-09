package org.example.servinet.core.domain.exception;

import java.nio.file.Path;

public class FileExistException extends RuntimeException {


    public FileExistException(Path path) {
        super(path.toString());
    }
  @Override
  public String getMessage() {
    return "No se pudo encontrar el archivo en la ruta" + super.getMessage();
  }
}
