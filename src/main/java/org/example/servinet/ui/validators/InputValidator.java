package org.example.servinet.ui.validators;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;

public class InputValidator {
    public static void limitLength(TextField field, int maxLength) {

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();

            return newText.length() <= maxLength ? change : null;
        };

        field.setTextFormatter(new TextFormatter<>(filter));
    }

    public static void numericField(TextField field, int maxLength) {

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();

            if (newText.length() > maxLength) {
                return null;
            }

            if (!newText.matches("\\d*")) {
                return null;
            }

            return change;
        };

        field.setTextFormatter(new TextFormatter<>(filter));
    }
}
