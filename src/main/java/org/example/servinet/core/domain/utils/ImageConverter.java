package org.example.servinet.core.domain.utils;

import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class ImageConverter {
    public static Image toImage(byte[] bytesImagen) {
        ByteArrayInputStream stream = new ByteArrayInputStream(bytesImagen);
        return new Image(stream);
    }

    public static Image toImage(Path image) throws IOException {
        try (InputStream inputStream = Files.newInputStream(image)) {
            return new Image(inputStream);
        }
    }

    public static byte[] toBytes(Path image) throws IOException {
        return Files.readAllBytes(image);
    }
}
