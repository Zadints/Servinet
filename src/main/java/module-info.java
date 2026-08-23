module org.example.mineide {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires java.net.http;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens org.example.mineide to javafx.fxml;
    opens org.example.mineide.controllers to javafx.fxml;
    opens org.example.mineide.controllers.center to javafx.fxml;

    exports org.example.mineide;
    exports org.example.mineide.controllers;
}