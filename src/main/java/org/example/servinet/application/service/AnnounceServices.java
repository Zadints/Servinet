package org.example.servinet.application.service;

import org.example.servinet.application.dto.AnnouncementDto;
import org.example.servinet.domain.exception.InvalidValueException;

import java.util.List;

public class AnnounceServices {

    private static final int TITLE_MAX = 20;
    private static final int DESCRIPTION_MAX = 150;
    private static final int COMMENT_MAX = 500;
    private static final long IMAGE_MAX_BYTES = 5L * 1024 * 1024;
    private static final List<String> PRIORITIES = List.of("Alta", "Media", "Baja");

    public static void validate(AnnouncementDto dto) {

        if (dto.getTitle() == null || dto.getTitle().isBlank())
            throw new InvalidValueException("El título no puede estar vacío.");
        if (dto.getTitle().trim().length() > TITLE_MAX)
            throw new InvalidValueException("El título no puede superar los " + TITLE_MAX + " caracteres.");

        if (dto.getDescription() == null || dto.getDescription().isBlank())
            throw new InvalidValueException("El contenido no puede estar vacío.");
        if (dto.getDescription().trim().length() > DESCRIPTION_MAX)
            throw new InvalidValueException("El contenido no puede superar los " + DESCRIPTION_MAX + " caracteres.");

        if (dto.getPriority() == null || !PRIORITIES.contains(dto.getPriority()))
            throw new InvalidValueException("Selecciona la prioridad del anuncio.");
    }

    public static long getImageMaxBytes(){
        return IMAGE_MAX_BYTES;
    }

}
