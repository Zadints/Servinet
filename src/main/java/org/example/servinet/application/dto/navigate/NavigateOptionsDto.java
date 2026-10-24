package org.example.servinet.application.dto.navigate;

import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public record NavigateOptionsDto(
        String fxmlName,
        double widthRatio,
        double heightRatio,

        boolean newWindow,
        Stage parentStage,
        Modality stageInitModality,
        StageStyle stageInitStyle,

        Color sceneSetFill,
        boolean stageSetMaximized,
        boolean stageShowAndAwait
) {}
