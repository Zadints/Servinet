module org.example.servinet {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires java.net.http;
    requires java.sql;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires annotations;
    requires org.yaml.snakeyaml;
    requires net.bytebuddy;
    requires de.mkammerer.argon2.nolibs;
    requires com.sun.jna;
    opens org.example.servinet to javafx.fxml;
    opens org.example.servinet.ui.controllers to javafx.fxml;
    opens org.example.servinet.ui.controllers.center to javafx.fxml;
    opens config;

    exports org.example.servinet.core.domain.enums;
    exports org.example.servinet;
    exports org.example.servinet.ui.controllers;
}