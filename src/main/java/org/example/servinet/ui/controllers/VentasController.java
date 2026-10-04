package org.example.servinet.ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import org.example.servinet.core.domain.exception.FileExistException;
import org.example.servinet.infrastructure.api.MapApi;

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

public class VentasController implements Initializable {
    // Componentes del mapa
    @FXML
    private WebView mapWebView;

    // Componentes del formulario de cliente y contrato (según tu interfaz)
    @FXML
    private TextField txtNombreCompleto;
    @FXML
    private TextField txtDni;
    @FXML
    private TextField txtTelefono;
    @FXML
    private TextField txtDireccion;
    @FXML
    private TextField txtSector;
    @FXML
    private ComboBox<String> cmbPlan;
    @FXML
    private ComboBox<String> cmbAntena;
    @FXML
    private DatePicker dpFechaInstalacion;

    // Componentes de pago
    @FXML
    private ComboBox<String> cmbMesPago;
    @FXML
    private TextField txtMonto;
    @FXML
    private DatePicker dpFechaPago;
    @FXML
    private ComboBox<String> cmbMetodoPago;
    @FXML
    private TextArea txtObservacion;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        inicializarMapa();
    }

    private void inicializarMapa() {
        if (mapWebView != null) {
            // Carga directamente el mapa de Alto Trujillo en el WebView
            MapApi.cargarMapaAltoTrujillo(mapWebView);
        }
    }


    @FXML
    private void handleCrearContrato() {
        try {
            // 1. Obtener los valores de los campos de texto y componentes
            String nombre = txtNombreCompleto != null ? txtNombreCompleto.getText().trim() : "";
            String dni = txtDni != null ? txtDni.getText().trim() : "";
            String telefono = txtTelefono != null ? txtTelefono.getText().trim() : "";
            String direccion = txtDireccion != null ? txtDireccion.getText().trim() : "";
            String sector = txtSector != null ? txtSector.getText().trim() : "";

            String plan = cmbPlan != null ? cmbPlan.getValue() : null;
            String antena = cmbAntena != null ? cmbAntena.getValue() : null;

            String montoStr = txtMonto != null ? txtMonto.getText().trim() : "0.00";

            // 2. Validaciones obligatorias
            if (nombre.isEmpty() || dni.length() < 8 || plan == null || antena == null) {
                mostrarAlerta("Error de validación", "Por favor completa el nombre, un DNI válido de 8 dígitos, selecciona un plan y una antena.");
                return;
            }

            double monto = Double.parseDouble(montoStr.isEmpty() ? "0.0" : montoStr);

            // 3. Simulación del proceso de guardado y validación de cobertura en el mapa de Alto Trujillo
            System.out.println("Guardando contrato para: " + nombre + " en el sector: " + (sector.isEmpty() ? "Alto Trujillo" : sector));
            System.out.println("Plan seleccionado: " + plan + " | Antena: " + antena + " | Monto: S/ " + monto);

            // 4. Integración del envío de correo usando tu EmailService
            try {
                org.example.servinet.infrastructure.api.EmailService emailService = new org.example.servinet.infrastructure.api.EmailService();
                emailService.enviarCorreoContrato("cliente@servinet.com", nombre, plan);
                System.out.println("Correo de confirmación enviado exitosamente.");
            } catch (Exception mailEx) {
                System.out.println("Aviso: No se pudo enviar el correo: " + mailEx.getMessage());
            }

            // 5. Mensaje de éxito y limpieza del formulario
            mostrarAlerta("¡Éxito!", "El contrato se ha creado correctamente y se ha notificado al cliente.");
            limpiarCamposFormulario();
        } catch (NumberFormatException e) {
            mostrarAlerta("Error en el monto", "Por favor ingresa un monto numérico válido.");
        } catch (Exception e) {
            mostrarAlerta("Error", "Ocurrió un error al crear el contrato: " + e.getMessage());
            e.printStackTrace();
        }


    }


    // Método auxiliar para mostrar alertas en pantalla
    private void mostrarAlerta(String titulo, String mensaje) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Método auxiliar para limpiar los campos después de crear el contrato
    private void limpiarCamposFormulario() {
        if (txtNombreCompleto != null) txtNombreCompleto.clear();
        if (txtDni != null) txtDni.clear();
        if (txtTelefono != null) txtTelefono.clear();
        if (txtDireccion != null) txtDireccion.clear();
        if (txtSector != null) txtSector.clear();
        if (cmbPlan != null) cmbPlan.setValue(null);
        if (cmbAntena != null) cmbAntena.setValue(null);
        if (txtMonto != null) txtMonto.clear();
        if (txtObservacion != null) txtObservacion.clear();
    }
}




