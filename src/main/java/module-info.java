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
    requires java.desktop;
    requires com.fasterxml.jackson.databind;
    requires javafx.graphics;
    requires jdk.jdi;
    requires com.microsoft.sqlserver.jdbc;
    opens org.example.servinet to javafx.fxml;
    opens org.example.servinet.ui.controllers to javafx.fxml;
    opens org.example.servinet.ui.controllers.center to javafx.fxml;
    opens org.example.servinet.ui.controllers.components to javafx.fxml;
    exports org.example.servinet.ui.controllers.components;
    opens config;

    exports org.example.servinet.application.dto.navigate;
    exports org.example.servinet.application.port.out;
    exports org.example.servinet.application.service;
    exports org.example.servinet.application.security;
    exports org.example.servinet.application.dto;
    exports org.example.servinet.application.usecase.staff;
    exports org.example.servinet.application.usecase.role;
    exports org.example.servinet.application.usecase.announce;
    exports org.example.servinet.bootstrap;
    exports org.example.servinet.domain.enums;
    exports org.example.servinet;
    exports org.example.servinet.ui.controllers;
    exports org.example.servinet.domain.entities;
    exports org.example.servinet.domain.entities.technicians;
    exports org.example.servinet.ui.navigator;
    exports org.example.servinet.infrastructure.dto;
    exports org.example.servinet.application.port.out.sql;
}