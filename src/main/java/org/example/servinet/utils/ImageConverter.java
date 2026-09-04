package org.example.servinet.utils;

import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class ImageConverter {
    public static Image toImage(String cadenaBase64){


        byte[] bytesImagen = Base64.getDecoder().decode(cadenaBase64);
        ByteArrayInputStream stream = new ByteArrayInputStream(bytesImagen);
        return new Image(stream);
    }
    public static String toBase64(Path image) throws IOException {
        byte[] bytes = Files.readAllBytes(image);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
