package org.example.servinet.ui.controllers.center;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.servinet.ui.controllers.IndexController;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import org.example.servinet.application.security.PermissionPolicy;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.ui.navigator.Navigator;

public class VentasMenuController {
    
    private final Navigator navigator;
    private final AuthorizationSecurity authorizationSecurity;
    public Button btnNewContract;

    public VentasMenuController(Navigator navigator, AuthorizationSecurity authorizationSecurity) {
        this.navigator = navigator;
        this.authorizationSecurity = authorizationSecurity;
    }


    @FXML

    public void initialize() {

    }

    private void mostrar(Node componente, boolean permitido) {

        if (componente == null) {
            return;
        }

        componente.setVisible(permitido);
        componente.setManaged(permitido);
    }

    private void funcionPendiente(String nombre) {

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);

        alerta.setTitle("Función pendiente");
        alerta.setHeaderText(nombre);
        alerta.setContentText(
                "Esta función todavía no tiene una " +
                        "pantalla implementada en ServiNet."
        );

        alerta.showAndWait();
    }

    private void cargarMetricasVentas() {
        String urlBD = "jdbc:sqlserver://localhost:1433;databaseName=servinet;integratedSecurity=true;encrypt=false;";

        // Consultas basadas en tus tablas `payments` y `orders`
        String sqlMes = "SELECT COUNT(p.payment_id) AS total_ventas, ISNULL(SUM(p.payment_amount), 0) AS monto_total " +
                "FROM payments p " +
                "WHERE MONTH(p.payment_date) = MONTH(GETDATE()) " +
                "  AND YEAR(p.payment_date) = YEAR(GETDATE())";

        String sqlHoy = "SELECT COUNT(payment_id) FROM payments WHERE CAST(payment_date AS DATE) = CAST(GETDATE() AS DATE)";

        try (Connection conn = DriverManager.getConnection(urlBD)) {
            // 1. Datos del Mes
            try (PreparedStatement stmt = conn.prepareStatement(sqlMes);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int totalMes = rs.getInt("total_ventas");
                    double montoTotal = rs.getDouble("monto_total");

                    if (lblVentasMes != null) {
                        lblVentasMes.setText(String.valueOf(totalMes));
                    }

                    // Estimación de comisión (ejemplo: 10% del total vendido)
                    if (lblComision != null) {
                        lblComision.setText(String.format("S/ %.2f", montoTotal * 0.10));
                    }

                    // Progreso del objetivo mensual (ejemplo basado en una meta de 20 ventas)
                    int metaMensual = 20;
                    int progreso = (int) (((double) totalMes / metaMensual) * 100);
                    if (lblObjetivoMensual != null) {
                        lblObjetivoMensual.setText(progreso + " %");
                    }
                }
            }

            // 2. Datos de Hoy
            try (PreparedStatement stmt = conn.prepareStatement(sqlHoy);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int totalHoy = rs.getInt(1);
                    if (lblVentasHoy != null) {
                        lblVentasHoy.setText(String.valueOf(totalHoy));
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private IndexController indexController;

    @FXML private VBox sectionResumenVentas;
    @FXML private VBox sectionAccionesVentas;

    @FXML private VBox cardNuevoContrato;
    @FXML private VBox cardRegistrarPago;
    @FXML private VBox cardGestionContratos;

    @FXML private VBox sectionOfertas;
    @FXML private VBox sectionContratosRecientes;
    @FXML private VBox sectionEstadisticas;
    @FXML private VBox sectionProspectos;

    @FXML private HBox accionesInferiores;

    @FXML private Label lblSinAccionesVentas;

    @FXML private Label lblVentasMes;
    @FXML private Label lblVentasHoy;
    @FXML private Label lblComision;
    @FXML private Label lblObjetivoMensual;
    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }


    @FXML
    protected void newContract() {

    
        if (!authorizationSecurity.hasPermission(
                Permission.SELL_CREATE_CONTRATE)) {

            new Alert(
                    Alert.AlertType.WARNING,
                    "No tienes permiso para crear contratos."
            ).showAndWait();

            return;
        } 
        

        try {
            navigator.openWindow(
                    "/org/example/servinet/center/sell-form.fxml",
                    (Stage) btnNewContract.getScene().getWindow(),
                    0.80,
                    0.80
            );
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    @FXML
    protected void showSelling() {
        funcionPendiente("Registrar pago");
    }

    @FXML
    protected void showOffers() {
        funcionPendiente("Ofertas");
    }

    @FXML
    protected void showContracts() {
        funcionPendiente("Mis contratos");
    }

    @FXML
    protected void selectOffer() {

        newContract();
    }

    @FXML
    protected void editContract() {
        funcionPendiente("Editar contrato");
    }

    @FXML
    protected void showStatistics() {
        funcionPendiente("Estadísticas de ventas");
    }

    @FXML
    protected void showProspects() {
        funcionPendiente("Clientes potenciales");
    }

}
