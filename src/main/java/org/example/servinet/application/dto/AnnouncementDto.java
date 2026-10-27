package org.example.servinet.application.dto;

import java.nio.file.Path;

public class AnnouncementDto {
    private final String title;
    private final String description;
    private final String priority;
    private final Path image;

    public AnnouncementDto(String title, String description, String priority, Path image) {
        this.title = title.trim();
        this.description = description.trim();
        this.priority = priority;
        this.image = image;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPriority() { return priority; }
    public Path getImage() { return image; }
}
