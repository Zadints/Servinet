package org.example.servinet.application.service;

import org.example.servinet.application.dto.AnnouncementDto;
import org.example.servinet.domain.exception.InvalidValueException;

import java.util.List;

public class AnnounceService {

    public static final int TITLE_MAX = 20;
    public static final int DESCRIPTION_MAX = 150;
    public static final int COMMENT_MAX = 500;
    public static final long IMAGE_MAX_BYTES = 5L * 1024 * 1024;
    public static final List<String> PRIORITIES = List.of("Alta", "Media", "Baja");

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
}
