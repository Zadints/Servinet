module org.example.servinet {
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
    requires annotations;

    opens org.example.servinet to javafx.fxml;
    opens org.example.servinet.controllers to javafx.fxml;
    opens org.example.servinet.controllers.center to javafx.fxml;

    exports org.example.servinet;
    exports org.example.servinet.controllers;
}