
package org.example.servinet.infrastructure.api;

import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import netscape.javascript.JSObject;
import org.example.servinet.ui.dto.GeoLocationDto;

import java.net.URL;

public final class LocationPicker {

    public GeoLocationDto show(Window owner) {

        Stage stage = new Stage();

        stage.initModality(Modality.APPLICATION_MODAL);

        if (owner != null) {
            stage.initOwner(owner);
        }

        stage.setTitle("Seleccionar ubicación");

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();

        engine.setOnAlert(event -> {
            System.out.println("[JS ALERT] " + event.getData());
        });

// Capturar errores de carga del documento
        engine.getLoadWorker().exceptionProperty().addListener(
                (observable, oldException, newException) -> {
                    if (newException != null) {
                        System.err.println("[WEBVIEW LOAD ERROR]");
                        newException.printStackTrace();
                    }
                }
        );

// Capturar errores JavaScript mediante el puente
        engine.getLoadWorker().stateProperty().addListener(
                (observable, oldState, newState) -> {

                    if (newState == Worker.State.SUCCEEDED) {

                        engine.executeScript("""
                    window.onerror = function(message, source, lineno, colno, error) {
                        if (window.javaBridge) {
                            window.javaBridge.onJavaScriptError(
                                String(message),
                                String(source),
                                lineno,
                                colno,
                                error ? String(error.stack) : ''
                            );
                        }

                        return false;
                    };

                    window.addEventListener('unhandledrejection', function(event) {
                        if (window.javaBridge) {
                            window.javaBridge.onJavaScriptError(
                                'Unhandled Promise Rejection: ' + String(event.reason),
                                '',
                                0,
                                0,
                                ''
                            );
                        }
                    });
                """);
                    }
                }
        );


        URL resource = getClass().getResource(
                "/org/example/servinet/forms/map/map-selector.html"
        );

        if (resource == null) {
            throw new IllegalStateException(
                    "No se encontró map-selector.html"
            );
        }

        final GeoLocationDto[] result = {null};

        engine.getLoadWorker().stateProperty().addListener(
                (observable, oldState, newState) -> {

                    System.out.println("Estado HTML: " + newState);

                    if (newState == Worker.State.SUCCEEDED) {

                        System.out.println("HTML cargado correctamente.");

                        try {

                            System.out.println(
                                    "Documento listo: " +
                                            engine.executeScript("document.readyState")
                            );

                            System.out.println(
                                    "Leaflet disponible: " +
                                            engine.executeScript("typeof L")
                            );

                            System.out.println(
                                    "Mapa disponible: " +
                                            engine.executeScript("typeof window.map")
                            );

                            JSObject jsWindow = (JSObject)
                                    engine.executeScript("window");

                            jsWindow.setMember(
                                    "javaBridge",
                                    new JavaBridge(stage, result)
                            );

                            System.out.println("JavaBridge registrado.");

                            engine.executeScript("""
                                if (window.mapReady === true && window.map) {
                                    window.invalidateMapSize();
                                }
                            """);

                        } catch (Exception exception) {
                            System.err.println(
                                    "Error inicializando comunicación JavaScript-Java:"
                            );
                            exception.printStackTrace();
                        }
                    }

                    if (newState == Worker.State.FAILED) {

                        System.err.println("Error cargando HTML:");

                        if (engine.getLoadWorker().getException() != null) {
                            engine.getLoadWorker()
                                    .getException()
                                    .printStackTrace();
                        }
                    }
                }
        );

        stage.setScene(new Scene(webView, 900, 650));

        stage.centerOnScreen();

        engine.load(resource.toExternalForm());

        stage.showAndWait();

        return result[0];
    }

    public static final class JavaBridge {

        private final Stage stage;
        private final GeoLocationDto[] result;

        public JavaBridge(Stage stage, GeoLocationDto[] result) {
            this.stage = stage;
            this.result = result;
        }

        public void onLocationSelected(double latitude, double longitude) {

            try {

                if (!Double.isFinite(latitude) ||
                        !Double.isFinite(longitude)) {
                    throw new IllegalArgumentException(
                            "Las coordenadas no son válidas."
                    );
                }

                result[0] = new GeoLocationDto(latitude, longitude);

                System.out.println("Ubicación recibida:");
                System.out.println("Latitud: " + latitude);
                System.out.println("Longitud: " + longitude);

                stage.close();

            } catch (Exception exception) {
                System.err.println("Error procesando ubicación:");
                exception.printStackTrace();
            }
        }

        public void log(String message) {
            System.out.println("[JS] " + message);
        }
    }
}
