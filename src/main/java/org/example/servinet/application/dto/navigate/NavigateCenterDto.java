package org.example.servinet.application.dto.navigate;

import javafx.scene.layout.BorderPane;

public record NavigateCenterDto(
        BorderPane container, String fileName
) {
}
